package protocol.bedrock

import lombok.AccessLevel
import java.lang.Exception
import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodType
import java.util.function.Supplier

@Immutable
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
class BedrockPacketCodec {
    @Getter
    private val protocolVersion = 0

    @Getter
    private val minecraftVersion: String? = null
    private val packetsById: Array<BedrockPacketDefinition<out BedrockPacket>>
    private val packetsByClass: Map<Class<out BedrockPacket>, BedrockPacketDefinition<out BedrockPacket>>? = null

    @Getter
    private val helper: BedrockPacketHelper? = null

    @Getter
    private val raknetProtocolVersion = 0
    @Throws(PacketSerializeException::class)
    fun tryDecode(buf: ByteBuf, id: Int, session: BedrockSession?): BedrockPacket {
        val definition = getPacketDefinition(id)
        val packet: BedrockPacket
        val serializer: BedrockPacketSerializer<BedrockPacket>
        if (definition == null) {
            val unknownPacket = UnknownPacket()
            unknownPacket.packetId = id
            packet = unknownPacket
            serializer = unknownPacket
        } else {
            packet = definition.getFactory().get()
            serializer = definition.getSerializer() as BedrockPacketSerializer<*>
        }
        try {
            serializer.deserialize(buf, helper, packet, session)
        } catch (e: Exception) {
            throw PacketSerializeException("Error whilst deserializing $packet", e)
        }
        if (log.isDebugEnabled() && buf.isReadable()) {
            log.debug(packet.javaClass.simpleName + " still has " + buf.readableBytes() + " bytes to read!")
        }
        return packet
    }

    @Throws(PacketSerializeException::class)
    fun tryEncode(buf: ByteBuf?, packet: BedrockPacket, session: BedrockSession?) {
        try {
            val serializer: BedrockPacketSerializer<BedrockPacket>
            if (packet is UnknownPacket) {
                serializer = packet as BedrockPacketSerializer<BedrockPacket>
            } else {
                val definition: BedrockPacketDefinition<*> = getPacketDefinition(packet.javaClass)
                serializer = definition.getSerializer()
            }
            serializer.serialize(buf, helper, packet, session)
        } catch (e: Exception) {
            throw PacketSerializeException("Error whilst serializing $packet", e)
        } finally {
            ReferenceCountUtil.release(packet)
        }
    }

    fun <T : BedrockPacket?> getPacketDefinition(packet: Class<T>?): BedrockPacketDefinition<T> {
        checkNotNull(packet, "packet")
        return packetsByClass!![packet] as BedrockPacketDefinition<T>
    }

    fun getPacketDefinition(id: Int): BedrockPacketDefinition<out BedrockPacket>? {
        return if (id < packetsById.size) {
            packetsById[id]
        } else null
    }

    fun getId(packet: BedrockPacket): Int {
        return (packet as? UnknownPacket)?.packetId ?: getId(packet.javaClass)
    }

    fun getId(clazz: Class<out BedrockPacket?>): Int {
        val definition = getPacketDefinition(clazz)
            ?: throw IllegalArgumentException("Packet ID for " + clazz.name + " does not exist.")
        return definition.getId()
    }

    fun toBuilder(): Builder {
        val builder = Builder()
        builder.packets.putAll(packetsByClass!!)
        builder.protocolVersion = protocolVersion
        builder.raknetProtocolVersion = raknetProtocolVersion
        builder.minecraftVersion = minecraftVersion
        builder.helper = helper
        return builder
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    class Builder {
        val packets: MutableMap<Class<out BedrockPacket>, BedrockPacketDefinition<out BedrockPacket>> =
            IdentityHashMap<Class<out BedrockPacket>, BedrockPacketDefinition<out BedrockPacket>>()
        var protocolVersion = -1
        var raknetProtocolVersion = 10
        var minecraftVersion: String? = null
        var helper: BedrockPacketHelper? = null
        fun <T : BedrockPacket?> registerPacket(
            packetClass: Class<T>,
            serializer: BedrockPacketSerializer<T>?,
            @Nonnegative id: Int
        ): Builder {
            checkArgument(id >= 0, "id cannot be negative")
            checkArgument(!packets.containsKey(packetClass), "Packet class already registered")
            val factory: Supplier<Any>
            factory = try {
                val lookup: MethodHandles.Lookup =
                    MethodHandlesExtensions.privateLookupIn(packetClass, MethodHandles.lookup())
                val handle: MethodHandle = lookup.findConstructor(packetClass, MethodType.methodType(Void.TYPE))
                LambdaFactory.createSupplier(handle)
            } catch (e: NoSuchMethodException) {
                throw IllegalArgumentException("Unable to find suitable constructor for packet factory", e)
            } catch (e: IllegalAccessException) {
                throw IllegalArgumentException("Unable to find suitable constructor for packet factory", e)
            }
            val info = BedrockPacketDefinition<T>(id, factory as Supplier<*>, serializer)
            packets[packetClass] = info
            return this
        }

        fun deregisterPacket(packetClass: Class<out BedrockPacket>?) {
            checkNotNull(packetClass, "packetClass")
            val info = packets.remove(packetClass)!!
        }

        fun protocolVersion(@Nonnegative protocolVersion: Int): Builder {
            checkArgument(protocolVersion >= 0, "protocolVersion cannot be negative")
            this.protocolVersion = protocolVersion
            return this
        }

        fun raknetProtocolVersion(@Nonnegative version: Int): Builder {
            checkArgument(version >= 0, "raknetProtocolVersion cannot be negative")
            raknetProtocolVersion = version
            return this
        }

        fun minecraftVersion(@Nonnull minecraftVersion: String): Builder {
            checkNotNull(minecraftVersion, "minecraftVersion")
            checkArgument(
                !minecraftVersion.isEmpty() && minecraftVersion.split("\\.").toTypedArray().size > 2,
                "Invalid minecraftVersion"
            )
            this.minecraftVersion = minecraftVersion
            return this
        }

        fun helper(@Nonnull helper: BedrockPacketHelper?): Builder {
            checkNotNull(helper, "helper")
            this.helper = helper
            return this
        }

        fun build(): BedrockPacketCodec {
            checkArgument(protocolVersion >= 0, "No protocol version defined")
            checkNotNull(minecraftVersion, "No Minecraft version defined")
            checkNotNull(helper, "helper cannot be null")
            var largestId = -1
            for (info in packets.values) {
                if (info.getId() > largestId) {
                    largestId = info.getId()
                }
            }
            checkArgument(largestId > -1, "Must have at least one packet registered")
            val packetsById: Array<BedrockPacketDefinition<out BedrockPacket>> = arrayOfNulls(largestId + 1)
            for (info in packets.values) {
                packetsById[info.getId()] = info
            }
            return BedrockPacketCodec(
                protocolVersion,
                minecraftVersion,
                packetsById,
                packets,
                helper,
                raknetProtocolVersion
            )
        }
    }

    companion object {
        private val log: InternalLogger = InternalLoggerFactory.getInstance(BedrockPacketCodec::class.java)
        @JvmStatic
        fun builder(): Builder {
            return Builder()
        }
    }
}