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

abstract class BedrockWrapperSerializer {
    /**
     * Compress packets to a buffer for sending
     *
     * @param buffer buffer to write batched packets to
     * @param codec  packet codec
     * @param level  compression level
     */
    abstract fun serialize(
        buffer: ByteBuf?,
        codec: BedrockPacketCodec,
        packets: Collection<BedrockPacket>,
        level: Int,
        session: BedrockSession?
    )

    /**
     * Decompress packets to handle
     *
     * @param buffer  buffer to write batched packets to
     * @param codec   packet codec
     * @param packets received packets
     */
    abstract fun deserialize(
        buffer: ByteBuf?,
        codec: BedrockPacketCodec,
        packets: MutableCollection<BedrockPacket?>,
        session: BedrockSession?
    )

    companion object {
        protected val log = InternalLoggerFactory.getInstance(BedrockWrapperSerializerV9_10::class.java)
    }
}