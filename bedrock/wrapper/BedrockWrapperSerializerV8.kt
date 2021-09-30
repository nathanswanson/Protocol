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

class BedrockWrapperSerializerV8 : BedrockWrapperSerializer() {
    override fun serialize(
        buffer: ByteBuf?,
        codec: BedrockPacketCodec,
        packets: Collection<BedrockPacket>,
        level: Int,
        session: BedrockSession?
    ) {
        val uncompressed = ByteBufAllocator.DEFAULT.ioBuffer(packets.size shl 3)
        try {
            for (packet in packets) {
                val packetBuffer = ByteBufAllocator.DEFAULT.ioBuffer()
                try {
                    val id = codec.getId(packet)
                    packetBuffer.writeByte(id)
                    packetBuffer.writeByte(packet.senderId)
                    packetBuffer.writeByte(packet.clientId)
                    codec.tryEncode(packetBuffer, packet, session)
                    VarInts.writeUnsignedInt(uncompressed, packetBuffer.readableBytes())
                    uncompressed.writeBytes(packetBuffer)
                } catch (e: PacketSerializeException) {
                    BedrockWrapperSerializer.Companion.log.error(
                        "Error occurred whilst encoding " + packet.javaClass.simpleName,
                        e
                    )
                } finally {
                    packetBuffer.release()
                }
            }
            ZLIB.deflate(uncompressed, buffer, level)
        } catch (e: DataFormatException) {
            throw RuntimeException("Unable to deflate buffer data", e)
        } finally {
            uncompressed.release()
        }
    }

    override fun deserialize(
        compressed: ByteBuf?,
        codec: BedrockPacketCodec,
        packets: MutableCollection<BedrockPacket?>,
        session: BedrockSession?
    ) {
        var decompressed: ByteBuf? = null
        try {
            decompressed = ZLIB.inflate(compressed, 2 * 1024 * 1024) // 2MBs
            while (decompressed.isReadable) {
                val length = VarInts.readUnsignedInt(decompressed)
                val packetBuffer = decompressed.readSlice(length)
                if (!packetBuffer.isReadable) {
                    throw DataFormatException("Packet cannot be empty")
                }
                try {
                    val packetId = packetBuffer.readUnsignedByte().toInt()
                    val packet = codec.tryDecode(packetBuffer, packetId, session)
                    packet.packetId = packetId
                    packet.senderId = packetBuffer.readUnsignedByte().toInt()
                    packet.clientId = packetBuffer.readUnsignedByte().toInt()
                    packets.add(packet)
                } catch (e: PacketSerializeException) {
                    BedrockWrapperSerializer.Companion.log.debug("Error occurred whilst decoding packet", e)
                    if (BedrockWrapperSerializer.Companion.log.isTraceEnabled()) {
                        BedrockWrapperSerializer.Companion.log.trace(
                            "Packet contents\n{}",
                            ByteBufUtil.prettyHexDump(packetBuffer.readerIndex(0))
                        )
                    }
                }
            }
        } catch (e: DataFormatException) {
            throw RuntimeException("Unable to inflate buffer data", e)
        } finally {
            decompressed?.release()
        }
    }

    companion object {
        val INSTANCE = BedrockWrapperSerializerV8()
        private val ZLIB: Zlib = Zlib.DEFAULT
    }
}