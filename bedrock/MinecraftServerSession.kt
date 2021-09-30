package protocol.bedrock

interface MinecraftServerSession<T : MinecraftPacket?> : MinecraftSession<T> {
    fun disconnect(reason: String?)
}