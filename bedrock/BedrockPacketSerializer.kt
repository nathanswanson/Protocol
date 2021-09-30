package protocol.bedrock

import protocol.bedrock.BedrockSession.hardcodedBlockingId
import protocol.bedrock.BedrockPacket.packetId
import protocol.bedrock.BedrockSession.checkForClosed
import protocol.bedrock.BedrockSession.tick
import protocol.bedrock.BedrockSession.isClosed
import protocol.bedrock.BedrockPong.ipv4Port
import protocol.bedrock.BedrockPong.ipv6Port
import protocol.bedrock.BedrockPacket
import com.nukkitx.protocol.serializer.PacketSerializer
import protocol.bedrock.BedrockPacketHelper
import io.netty.buffer.ByteBuf
import protocol.bedrock.BedrockSession
import com.nukkitx.protocol.util.Int2ObjectBiMap
import it.unimi.dsi.fastutil.objects.Object2IntMap
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import com.nukkitx.protocol.bedrock.data.structure.StructureSettings
import com.nukkitx.protocol.bedrock.data.skin.SerializedSkin
import com.nukkitx.protocol.bedrock.data.skin.AnimationData
import com.nukkitx.protocol.bedrock.data.skin.ImageData
import network.common.VarInts
import io.netty.util.AsciiString
import java.util.UUID
import java.util.function.BiFunction
import com.nukkitx.protocol.bedrock.util.TriConsumer
import com.nukkitx.protocol.util.TriFunction
import com.nukkitx.protocol.util.QuadConsumer
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import java.util.function.BiConsumer
import io.netty.buffer.ByteBufInputStream
import java.io.IOException
import java.lang.RuntimeException
import io.netty.buffer.ByteBufOutputStream
import com.nukkitx.protocol.bedrock.packet.InventoryTransactionPacket
import java.util.Objects
import java.lang.UnsupportedOperationException
import com.nukkitx.protocol.bedrock.data.inventory.stackrequestactions.StackRequestActionType
import io.netty.util.internal.logging.InternalLogger
import io.netty.util.internal.logging.InternalLoggerFactory
import protocol.bedrock.BedrockPacketSerializer
import protocol.bedrock.BedrockPacketDefinition
import kotlin.Throws
import com.nukkitx.protocol.bedrock.exception.PacketSerializeException
import com.nukkitx.protocol.bedrock.packet.UnknownPacket
import protocol.bedrock.BedrockPacketCodec
import io.netty.util.ReferenceCountUtil
import java.lang.IllegalArgumentException
import java.util.IdentityHashMap
import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodHandle
import java.lang.NoSuchMethodException
import java.lang.IllegalAccessException
import network.raknet.RakNetSession
import kotlin.jvm.JvmOverloads
import java.net.InetSocketAddress
import io.netty.channel.EventLoopGroup
import protocol.bedrock.Bedrock
import network.raknet.RakNetClient
import protocol.bedrock.BedrockClientSession
import java.util.concurrent.TimeUnit
import java.util.concurrent.CompletableFuture
import protocol.bedrock.BedrockPong
import java.net.Inet4Address
import java.net.Inet6Address
import java.util.concurrent.CompletionStage
import network.raknet.RakNetClientSession
import protocol.bedrock.wrapper.BedrockWrapperSerializer
import protocol.bedrock.wrapper.BedrockWrapperSerializers

interface BedrockPacketSerializer<T : BedrockPacket?> : PacketSerializer<T, BedrockPacketHelper?> {
    override fun serialize(buffer: ByteBuf?, helper: BedrockPacketHelper?, packet: T) {}
    fun serialize(buffer: ByteBuf?, helper: BedrockPacketHelper?, packet: T, session: BedrockSession?) {
        this.serialize(buffer, helper, packet)
    }

    override fun deserialize(buffer: ByteBuf?, helper: BedrockPacketHelper?, packet: T) {}
    fun deserialize(buffer: ByteBuf?, helper: BedrockPacketHelper?, packet: T, session: BedrockSession?) {
        this.deserialize(buffer, helper, packet)
    }
}