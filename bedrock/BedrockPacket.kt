package protocol.bedrock

import protocol.bedrock.handler.*

abstract class BedrockPacket(var packetId: Int,var senderId: Int,var clientId: Int) : MinecraftPacket {
    abstract fun handle(handler: BedrockPacketHandler?): Boolean
    abstract val packetType: BedrockPacketType?
}