package protocol.bedrock

import io.netty.channel.socket.DatagramPacket
import javax.annotation.ParametersAreNonnullByDefault

@ParametersAreNonnullByDefault
interface BedrockServerEventHandler {
    fun onConnectionRequest(address: InetSocketAddress?, realAddress: InetSocketAddress?): Boolean {
        return onConnectionRequest(address)
    }

    fun onConnectionRequest(address: InetSocketAddress?): Boolean {
        throw UnsupportedOperationException("BedrockServerEventHandler#onConnectionRequest is not implemented")
    }

    @Nullable
    fun onQuery(address: InetSocketAddress?): BedrockPong?
    fun onSessionCreation(serverSession: BedrockServerSession?)
    fun onUnhandledDatagram(ctx: ChannelHandlerContext?, packet: DatagramPacket?) {}
}