package protocol.bedrock

import protocol.bedrock.wrapper.BedrockWrapperSerializer
import io.netty.channel.EventLoop
import network.raknet.RakNetSession

class BedrockClientSession internal constructor(
    connection: RakNetSession,
    eventLoop: EventLoop?,
    serializer: BedrockWrapperSerializer?
) : BedrockSession(connection, eventLoop!!, serializer) {
    override fun disconnect() {
        checkForClosed()
        connection.disconnect()
    }
}