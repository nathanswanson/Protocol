package protocol.bedrock

import protocol.bedrock.BedrockPacket

fun interface BedrockPacketFactory<T : BedrockPacket?> {
    fun newInstance(): BedrockPacket
    val packetClass: Class<BedrockPacket?>?
        get() = newInstance().javaClass as Class<BedrockPacket?>
}