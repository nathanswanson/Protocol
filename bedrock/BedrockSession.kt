package protocol.bedrock

import com.nukkitx.natives.sha256.Sha256
import io.netty.channel.EventLoop
import io.netty.util.internal.PlatformDependent
import java.lang.Exception
import java.nio.ByteBuffer
import java.util.*
import java.util.function.Consumer

abstract class BedrockSession internal constructor(
    connection: SessionConnection<ByteBuf?>,
    eventLoop: EventLoop,
    serializer: BedrockWrapperSerializer
) : MinecraftSession<BedrockPacket?> {
    private val disconnectHandlers: MutableSet<Consumer<DisconnectReason>> =
        Collections.newSetFromMap<Consumer<DisconnectReason>>(ConcurrentHashMap<Consumer<DisconnectReason>, Boolean>())
    private val queuedPackets = PlatformDependent.newMpscQueue<BedrockPacket>()
    private val sentEncryptedPacketCount: AtomicLong = AtomicLong()
    private val wrapperSerializer: BedrockWrapperSerializer
    val eventLoop: EventLoop
    @JvmField
    val connection: SessionConnection<ByteBuf>
    private var packetCodec: BedrockPacketCodec = BedrockCompat.COMPAT_CODEC
    var packetHandler: BedrockPacketHandler? = null
    private var batchHandler: BatchHandler = DefaultBatchHandler.INSTANCE
    private var encryptionCipher: Cipher? = null
    private var decryptionCipher: Cipher? = null
    private var agreedKey: SecretKey? = null
    var compressionLevel: Int = Deflater.DEFAULT_COMPRESSION

    @Volatile
    private var closed = false

    @Volatile
    var isLogging = true
    val hardcodedBlockingId: AtomicInteger = AtomicInteger(-1)

    companion object {
        private val log: InternalLogger = InternalLoggerFactory.getInstance(BedrockSession::class.java)
        private val HASH_LOCAL: ThreadLocal<Sha256>? = null

        init {
            // Required for Android API versions prior to 26.
            HASH_LOCAL = object : ThreadLocal<Sha256>() {
                override fun initialValue(): Sha256 {
                    return Natives.SHA_256.get()
                }
            }
        }
    }

    fun setPacketCodec(packetCodec: BedrockPacketCodec?) {
        this.packetCodec = Objects.requireNonNull<BedrockPacketCodec>(packetCodec, "packetCodec")
    }

    fun checkForClosed() {
        check(!closed) { "Connection has been closed" }
    }

    override fun sendPacket(@Nonnull packet: BedrockPacket) {
        checkPacket(packet)
        queuedPackets.add(packet)
    }

    override fun sendPacketImmediately(@Nonnull packet: BedrockPacket) {
        checkPacket(packet)
        this.sendWrapped(listOf(packet), !packet.javaClass.isAnnotationPresent(NoEncryption::class.java))
    }

    private fun checkPacket(packet: BedrockPacket) {
        checkForClosed()
        Objects.requireNonNull<BedrockPacket>(packet, "packet")
        if (log.isTraceEnabled() && isLogging) {
            val to: String = connection.getAddress().toString()
            log.trace("Outbound {}: {}", to, packet)
        }

        // Verify that the packet ID exists.
        packetCodec.getId(packet)
    }

    fun sendWrapped(packets: Collection<BedrockPacket?>?, encrypt: Boolean) {
        val compressed: ByteBuf = ByteBufAllocator.DEFAULT.ioBuffer()
        try {
            wrapperSerializer.serialize(compressed, packetCodec, packets, compressionLevel, this)
            this.sendWrapped(compressed, encrypt)
        } catch (e: Exception) {
            log.error("Unable to compress packets", e)
        } finally {
            if (compressed != null) {
                compressed.release()
            }
        }
    }

    @Synchronized
    fun sendWrapped(compressed: ByteBuf, encrypt: Boolean) {
        Objects.requireNonNull<ByteBuf>(compressed, "compressed")
        try {
            val finalPayload: ByteBuf = ByteBufAllocator.DEFAULT.ioBuffer(1 + compressed.readableBytes() + 8)
            finalPayload.writeByte(0xfe) // Wrapped packet ID
            if (encryptionCipher != null && encrypt) {
                val trailer = ByteBuffer.wrap(generateTrailer(compressed))
                val outBuffer: ByteBuffer = finalPayload.internalNioBuffer(1, compressed.readableBytes() + 8)
                val inBuffer: ByteBuffer =
                    compressed.internalNioBuffer(compressed.readerIndex(), compressed.readableBytes())
                encryptionCipher.update(inBuffer, outBuffer)
                encryptionCipher.update(trailer, outBuffer)
                finalPayload.writerIndex(finalPayload.writerIndex() + compressed.readableBytes() + 8)
            } else {
                finalPayload.writeBytes(compressed)
            }
            connection.send(finalPayload)
        } catch (e: GeneralSecurityException) {
            throw RuntimeException("Unable to encrypt package", e)
        }
    }

    fun tick() {
        eventLoop.execute { onTick() }
    }

    private fun onTick() {
        if (closed) {
            return
        }
        sendQueued()
    }

    private fun sendQueued() {
        var packet: BedrockPacket
        var toBatch: MutableList<BedrockPacket?> = ObjectArrayList<BedrockPacket>()
        while (queuedPackets.poll().also { packet = it } != null) {
            if (packet.javaClass.isAnnotationPresent(NoEncryption::class.java)) {
                // We hit a unencryptable packet. Send the current wrapper and then send the unencryptable packet.
                if (!toBatch.isEmpty()) {
                    this.sendWrapped(toBatch, true)
                    toBatch = ObjectArrayList<BedrockPacket>()
                }
                this.sendPacketImmediately(packet)
                continue
            }
            toBatch.add(packet)
        }
        if (!toBatch.isEmpty()) {
            this.sendWrapped(toBatch, true)
        }
    }

    @Synchronized
    fun enableEncryption(@Nonnull secretKey: SecretKey) {
        checkForClosed()
        log.debug("Encryption enabled.")
        Objects.requireNonNull<SecretKey>(secretKey, "secretKey")
        require(secretKey.getAlgorithm() == "AES") { "Invalid key algorithm" }
        check(!(encryptionCipher != null || decryptionCipher != null)) { "Encryption has already been enabled" }
        agreedKey = secretKey
        val useGcm: Boolean = packetCodec.getProtocolVersion() > 428
        encryptionCipher = EncryptionUtils.createCipher(useGcm, true, secretKey)
        decryptionCipher = EncryptionUtils.createCipher(useGcm, false, secretKey)
    }

    private fun generateTrailer(buf: ByteBuf): ByteArray {
        val hash: Sha256 = HASH_LOCAL!!.get()
        val counterBuf: ByteBuf = ByteBufAllocator.DEFAULT.directBuffer(8)
        return try {
            counterBuf.writeLongLE(sentEncryptedPacketCount.getAndIncrement())
            val keyBuffer = ByteBuffer.wrap(agreedKey.getEncoded())
            hash.update(counterBuf.internalNioBuffer(0, 8))
            hash.update(buf.internalNioBuffer(buf.readerIndex(), buf.readableBytes()))
            hash.update(keyBuffer)
            val digested: ByteArray = hash.digest()
            Arrays.copyOf(digested, 8)
        } finally {
            counterBuf.release()
            hash.reset()
        }
    }

    val isEncrypted: Boolean
        get() = encryptionCipher != null

    abstract override fun disconnect()
    fun close(reason: DisconnectReason?) {
        checkForClosed()
        closed = true
        // Free native resources if required
//        if (this.encryptionCipher != null) {
//            this.encryptionCipher.free();
//        }
//        if (this.decryptionCipher != null) {
//            this.decryptionCipher.free();
//        }

        // Destroy secret key
        if (agreedKey != null && !agreedKey.isDestroyed()) {
            try {
                agreedKey.destroy()
            } catch (e: DestroyFailedException) {
                // Ignore - throws exception by default
            }
        }
        for (disconnectHandler in disconnectHandlers) {
            disconnectHandler.accept(reason)
        }
    }

    fun onWrappedPacket(batched: ByteBuf) {
        try {
            if (isEncrypted) {
                // This method only supports contiguous buffers, not composite.
                val inBuffer: ByteBuffer = batched.internalNioBuffer(batched.readerIndex(), batched.readableBytes())
                val outBuffer = inBuffer.duplicate()
                // Copy-safe so we can use the same buffer.
                decryptionCipher.update(inBuffer, outBuffer)

                // TODO: Maybe verify the checksum?
                batched.writerIndex(batched.writerIndex() - 8)
            }
            batched.markReaderIndex()
            if (batched.isReadable()) {
                val packets: List<BedrockPacket> = ObjectArrayList<BedrockPacket>()
                wrapperSerializer.deserialize(batched, packetCodec, packets, this)
                batchHandler.handle(this, batched, packets)
            }
        } catch (ignore: GeneralSecurityException) {
        } catch (e: PacketSerializeException) {
            log.warn("Error whilst decoding packets", e)
        }
    }

    override val address: InetSocketAddress
        get() = connection.getAddress()
    override val realAddress: InetSocketAddress
        get() = connection.getRealAddress()

    override fun isClosed(): Boolean {
        return connection.isClosed()
    }

    fun getPacketCodec(): BedrockPacketCodec {
        return packetCodec
    }

    fun getBatchHandler(): BatchHandler {
        return batchHandler
    }

    fun setBatchHandler(batchHandler: BatchHandler?) {
        this.batchHandler = Objects.requireNonNull<BatchHandler>(batchHandler, "batchHandler")
    }

    fun addDisconnectHandler(disconnectHandler: Consumer<DisconnectReason>) {
        Objects.requireNonNull<Consumer<DisconnectReason>>(disconnectHandler, "disconnectHandler")
        disconnectHandlers.add(disconnectHandler)
    }

    override val latency: Long
        get() = connection.getPing()

    fun getConnection(): SessionConnection<ByteBuf> {
        return connection
    } //    @ParametersAreNonnullByDefault

    //    abstract class BedrockSessionListener implements RakNetSessionListener {
    //
    //        @Override
    //        public void onEncapsulated(EncapsulatedPacket packet) {
    //            if (BedrockSession.this.connection.getState() != RakNetState.CONNECTED) {
    //                // We shouldn't be receiving packets till the connection is full established.
    //                return;
    //            }
    //            ByteBuf buffer = packet.getBuffer();
    //
    //            int packetId = buffer.readUnsignedByte();
    //            if (packetId == 0xfe /* Wrapper packet */) {
    //                BedrockSession.this.onWrappedPacket(buffer);
    //            }
    //        }
    //
    //        @Override
    //        public void onDirect(ByteBuf buf) {
    //            // We shouldn't be receiving direct datagram messages from the client whilst they are connected.
    //        }
    //    }
    init {
        this.connection = connection
        this.eventLoop = eventLoop
        wrapperSerializer = serializer
    }
}