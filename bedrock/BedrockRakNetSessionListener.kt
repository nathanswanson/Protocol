package protocol.bedrock

import com.nukkitx.network.util.DisconnectReason

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
@ParametersAreNonnullByDefault
abstract class BedrockRakNetSessionListener : RakNetSessionListener {
    val session: BedrockSession? = null
    private val connection: RakNetSession? = null
    override fun onEncapsulated(packet: EncapsulatedPacket) {
        if (connection.getState() != RakNetState.CONNECTED) {
            // We shouldn't be receiving packets till the connection is full established.
            return
        }
        val buffer: ByteBuf = packet.getBuffer()
        val packetId: Int = buffer.readUnsignedByte().toInt()
        if (packetId == 0xfe && buffer.isReadable()) {
            // Wrapper packet
            session!!.onWrappedPacket(buffer)
        }
    }

    override fun onDirect(buf: ByteBuf) {
        // We shouldn't be receiving direct datagram messages from the client whilst they are connected.
    }

    @ParametersAreNonnullByDefault
    class Client internal constructor(
        session: BedrockClientSession?, connection: RakNetSession?, private val client: BedrockClient,
        future: CompletableFuture<BedrockClientSession?>
    ) : BedrockRakNetSessionListener(session, connection) {
        var future: CompletableFuture<BedrockClientSession>
        override fun onSessionChangeState(state: RakNetState) {
            if (state == RakNetState.CONNECTED && future != null) {
                future.complete(session as BedrockClientSession?)
                future = null
            }
        }

        override fun onDisconnect(reason: DisconnectReason) {
            session!!.close(reason)
            if (future != null && !future.isDone()) {
                future.completeExceptionally(ConnectionFailedException(reason))
            }
            client.session = null
        }

        init {
            this.future = future
        }
    }

    @ParametersAreNonnullByDefault
    class Server internal constructor(
        session: BedrockServerSession?,
        connection: RakNetSession?,
        private val server: BedrockServer
    ) : BedrockRakNetSessionListener(session, connection) {
        override fun onSessionChangeState(state: RakNetState) {
            if (state == RakNetState.CONNECTED) {
                server.sessions.add(session as BedrockServerSession?)
                val handler = server.handler
                handler?.onSessionCreation(session)
            }
        }

        override fun onDisconnect(reason: DisconnectReason) {
            session!!.close(reason)
            server.sessions.remove(session as BedrockServerSession?)
        }
    }
}