package protocol.bedrock

import io.netty.channel.EventLoopGroup
import network.common.util.EventLoops
import network.raknet.RakNetClient
import network.raknet.RakNetClientSession
import protocol.bedrock.wrapper.BedrockWrapperSerializer
import protocol.bedrock.wrapper.BedrockWrapperSerializers
import java.net.Inet4Address
import java.net.Inet6Address
import java.net.InetSocketAddress
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionStage
import java.util.concurrent.TimeUnit
import java.util.function.Function

class BedrockClient @JvmOverloads constructor(
    bindAddress: InetSocketAddress?,
    eventLoopGroup: EventLoopGroup? = EventLoops.commonGroup()
) : Bedrock(eventLoopGroup) {
    private val rakNetClient: RakNetClient
    var session: BedrockClientSession? = null
    override fun onTick() {
        if (session != null) {
            session!!.tick()
        }
    }

    override val rakNet: RakNetClient
        get() = rakNetClient

    fun setRakNetVersion(version: Int) {
        rakNetClient.setProtocolVersion(version)
    }

    override fun close(force: Boolean) {
        if (session != null && !session!!.isClosed()) {
            session!!.disconnect()
        }
        rakNetClient.close()
        tickFuture.cancel(false)
    }

    @JvmOverloads
    fun connect(
        address: InetSocketAddress,
        timeout: Long = 10,
        unit: TimeUnit? = TimeUnit.SECONDS
    ): CompletableFuture<BedrockClientSession> {
        return ping(address, timeout, unit).thenApply<InetSocketAddress>(Function { pong: BedrockPong ->
            val port: Int
            port = if (address.getAddress() is Inet4Address && pong.ipv4Port != -1) {
                pong.ipv4Port
            } else if (address.getAddress() is Inet6Address && pong.ipv6Port != -1) {
                pong.ipv6Port
            } else {
                address.getPort()
            }
            InetSocketAddress(address.getAddress(), port)
        })
            .thenCompose<BedrockClientSession>(Function<InetSocketAddress, CompletionStage<BedrockClientSession>> { address: InetSocketAddress? ->
                directConnect(address)
            })
    }

    fun directConnect(address: InetSocketAddress?): CompletableFuture<BedrockClientSession> {
        val future: CompletableFuture<BedrockClientSession> = CompletableFuture<BedrockClientSession>()
        val connection: RakNetClientSession = rakNetClient.connect(address)
        val serializer: BedrockWrapperSerializer =
            BedrockWrapperSerializers.getSerializer(connection.protocolVersion)
        session = BedrockClientSession(connection, eventLoopGroup!!.next(), serializer)
        val listener = BedrockRakNetSessionListener.Client(
            session,
            connection, this, future
        )
        //connection.setListener(listener)
        return future
    }

    @JvmOverloads
    fun ping(
        address: InetSocketAddress?,
        timeout: Long = 10,
        unit: TimeUnit? = TimeUnit.SECONDS
    ): CompletableFuture<BedrockPong> {
        return rakNetClient.ping(address, timeout, unit).thenApply<BedrockPong>(BedrockPong::fromRakNet)
    }

    init {
        rakNetClient = RakNetClient(bindAddress, eventLoopGroup)
    }
}