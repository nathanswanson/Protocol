package protocol.bedrock

import java.util.concurrent.CompletableFuture
import java.lang.Void
import java.net.InetSocketAddress

interface MinecraftInterface {
    fun bind(): CompletableFuture<Void?>?
    fun close()
    val bindAddress: InetSocketAddress
}