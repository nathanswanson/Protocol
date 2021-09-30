package protocol.bedrock.wrapper

import protocol.bedrock.BedrockPacketCodec.getId
import protocol.bedrock.BedrockPacketCodec.tryEncode
import protocol.bedrock.BedrockPacketCodec.tryDecode
import protocol.bedrock.BedrockPacket.packetId
import protocol.bedrock.BedrockPacket.senderId
import protocol.bedrock.BedrockPacket.clientId
import io.netty.buffer.ByteBuf
import protocol.bedrock.BedrockPacketCodec
import protocol.bedrock.BedrockPacket
import protocol.bedrock.BedrockSession
import io.netty.util.internal.logging.InternalLogger
import io.netty.util.internal.logging.InternalLoggerFactory
import protocol.bedrock.wrapper.BedrockWrapperSerializerV9_10
import it.unimi.dsi.fastutil.ints.Int2ObjectMap
import protocol.bedrock.wrapper.BedrockWrapperSerializer
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import protocol.bedrock.wrapper.BedrockWrapperSerializers
import protocol.bedrock.wrapper.BedrockWrapperSerializerV7
import protocol.bedrock.wrapper.BedrockWrapperSerializerV8
import io.netty.buffer.ByteBufAllocator
import network.common.VarInts
import com.nukkitx.protocol.bedrock.exception.PacketSerializeException
import java.util.zip.DataFormatException
import java.lang.RuntimeException
import io.netty.buffer.ByteBufUtil
import com.nukkitx.protocol.util.Zlib

object BedrockWrapperSerializers {
    private val SERIALIZERS: Int2ObjectMap<BedrockWrapperSerializer> = Int2ObjectOpenHashMap()
    fun getSerializer(protocolVersion: Int): BedrockWrapperSerializer {
        return SERIALIZERS[protocolVersion]
    }

    init {
        SERIALIZERS[7] = BedrockWrapperSerializerV7.Companion.INSTANCE
        SERIALIZERS[8] = BedrockWrapperSerializerV8.Companion.INSTANCE
        SERIALIZERS[9] = BedrockWrapperSerializerV9_10.Companion.V9
        SERIALIZERS[10] = BedrockWrapperSerializerV9_10.Companion.V10
        SERIALIZERS.defaultReturnValue(BedrockWrapperSerializerV9_10.Companion.V9)
    }
}