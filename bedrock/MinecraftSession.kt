package protocol.bedrock

import protocol.bedrock.MinecraftPacket
import java.net.InetSocketAddress

interface MinecraftSession<T : MinecraftPacket?> {
    val isClosed: Boolean
    fun disconnect()
    val address: InetSocketAddress?
    val realAddress: InetSocketAddress?
        get() = address

    fun sendPacket(packet: T)
    fun sendPacketImmediately(packet: T)
    val latency: Long
}