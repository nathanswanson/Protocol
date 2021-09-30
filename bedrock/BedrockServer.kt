package protocol.bedrock

import kotlin.jvm.JvmOverloads
import java.net.InetSocketAddress
import io.netty.channel.EventLoopGroup
import network.common.util.EventLoops
import network.raknet.RakNetServer
import java.util.Collections
import java.util.concurrent.ConcurrentHashMap
import network.raknet.RakNetServerListener
import network.raknet.RakNetServerSession
import protocol.bedrock.wrapper.BedrockWrapperSerializers
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.socket.DatagramPacket

class BedrockServer @JvmOverloads constructor(
    bindAddress: InetSocketAddress?,
    maxThreads: Int = 1,
    eventLoopGroup: EventLoopGroup? = EventLoops.commonGroup(),
    allowProxyProtocol: Boolean = false
) : Bedrock(
    eventLoopGroup!!
) {
    override val rakNet: RakNetServer? = null
    val sessions = Collections.newSetFromMap(ConcurrentHashMap<BedrockServerSession?, Boolean>())
    var handler: BedrockServerEventHandler? = null
    override fun close(force: Boolean) {
        this.close("disconnect.disconnected")
    }

    fun close(reason: String?) {
        for (session in sessions) {
            session!!.disconnect(reason)
        }
        rakNet!!.close()
        tickFuture.cancel(false)
    }

    val isClosed: Boolean
        get() = rakNet!!.isClosed

    override fun onTick() {
        for (session in sessions) {
            session!!.tick()
        }
    }

    private inner class BedrockServerListener : RakNetServerListener {
        fun onConnectionRequest(address: InetSocketAddress?, realAddress: InetSocketAddress?): Boolean {
            return handler == null || handler!!.onConnectionRequest(address, realAddress)
        }

        override fun onConnectionRequest(address: InetSocketAddress): Boolean {
            return false
        }

        override fun onQuery(address: InetSocketAddress): ByteArray {
            if (handler != null) {
                val pong = handler!!.onQuery(address)
                if (pong != null) {
                    pong.serverId = rakNet!!.guid
                    return pong.toRakNet()
                }
            }
            return null
        }

        override fun onSessionCreation(connection: RakNetServerSession) {
            val serializer = BedrockWrapperSerializers.getSerializer(connection.protocolVersion)
            val session = BedrockServerSession(connection, eventLoopGroup.next(), serializer)
            //connection.setListener(new BedrockRakNetSessionListener.Server(session, connection, BedrockServer.this));
        }

        override fun onUnhandledDatagram(ctx: ChannelHandlerContext, packet: DatagramPacket) {
            if (handler != null) {
                handler!!.onUnhandledDatagram(ctx, packet)
            }
        }
    }
}