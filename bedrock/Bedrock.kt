package protocol.bedrock

import io.netty.bootstrap.Bootstrap
import io.netty.channel.EventLoopGroup
import network.raknet.RakNet
import network.raknet.RakNetServer
import java.net.InetSocketAddress
import java.util.concurrent.CompletableFuture
import java.lang.Void
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

abstract class Bedrock internal constructor(val eventLoopGroup: EventLoopGroup?) : MinecraftInterface {
    @JvmField
    val tickFuture: ScheduledFuture<*> = TODO()
    protected abstract fun onTick()
    open val rakNet: RakNet = TODO()

    override val bindAddress: InetSocketAddress
        get() = rakNet.bindAddress

    val bootstrap: Bootstrap
        get() = rakNet.bootstrap

    override fun bind(): CompletableFuture<Void?>? {
        return rakNet.bind()
    }

    override fun close() {
        this.close(false)
    }

    abstract fun close(force: Boolean)

    init {
        tickFuture = eventLoopGroup!!.scheduleAtFixedRate({ onTick() }, 50, 50, TimeUnit.MILLISECONDS)
    }
}