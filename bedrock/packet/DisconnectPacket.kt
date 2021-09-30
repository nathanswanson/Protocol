package protocol.bedrock.packet

import protocol.bedrock.BedrockPacket
import protocol.bedrock.BedrockPacketType
import protocol.bedrock.handler.BedrockPacketHandler

class DisconnectPacket(var messageSkipped: Boolean,var kickMessage: String?) : BedrockPacket(1,1,1) {

    override val packetType: BedrockPacketType
        get() = BedrockPacketType.DISCONNECT

    override fun handle(handler: BedrockPacketHandler?): Boolean {
        return handler!!.handle(this)
    }
}