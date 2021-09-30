package protocol.bedrock

import protocol.bedrock.BedrockPong.serverId
import protocol.bedrock.BedrockPong.toRakNet
import protocol.bedrock.packet.DisconnectPacket.messageSkipped
import protocol.bedrock.packet.DisconnectPacket.kickMessage
import network.raknet.RakNetSessionListener
import protocol.bedrock.BedrockSession
import network.raknet.RakNetSession
import network.raknet.EncapsulatedPacket
import network.raknet.RakNetState
import io.netty.buffer.ByteBuf
import protocol.bedrock.BedrockClientSession
import protocol.bedrock.BedrockClient
import java.util.concurrent.CompletableFuture
import protocol.bedrock.BedrockRakNetSessionListener
import com.nukkitx.protocol.bedrock.exception.ConnectionFailedException
import protocol.bedrock.BedrockServerSession
import protocol.bedrock.BedrockServer
import protocol.bedrock.BedrockServerEventHandler
import kotlin.jvm.JvmOverloads
import java.net.InetSocketAddress
import io.netty.channel.EventLoopGroup
import network.common.util.EventLoops
import protocol.bedrock.Bedrock
import network.raknet.RakNetServer
import java.util.Collections
import java.util.concurrent.ConcurrentHashMap
import network.raknet.RakNetServerListener
import protocol.bedrock.BedrockPong
import network.raknet.RakNetServerSession
import protocol.bedrock.wrapper.BedrockWrapperSerializer
import protocol.bedrock.wrapper.BedrockWrapperSerializers
import io.netty.channel.ChannelHandlerContext
import java.lang.UnsupportedOperationException
import protocol.bedrock.MinecraftServerSession
import protocol.bedrock.BedrockPacket
import protocol.bedrock.packet.DisconnectPacket
import network.common.SessionConnection
import protocol.bedrock.MinecraftSession
import network.common.util.DisconnectReason
import java.util.concurrent.atomic.AtomicLong
import protocol.bedrock.BedrockPacketCodec
import com.nukkitx.protocol.bedrock.compat.BedrockCompat
import protocol.bedrock.handler.BedrockPacketHandler
import com.nukkitx.protocol.bedrock.handler.BatchHandler
import com.nukkitx.protocol.bedrock.handler.DefaultBatchHandler
import javax.crypto.Cipher
import javax.crypto.SecretKey
import java.util.zip.Deflater
import kotlin.jvm.Volatile
import java.util.concurrent.atomic.AtomicInteger
import io.netty.util.internal.logging.InternalLogger
import io.netty.util.internal.logging.InternalLoggerFactory
import java.lang.ThreadLocal
import java.util.Objects
import java.lang.IllegalStateException
import com.nukkitx.protocol.bedrock.annotation.NoEncryption
import io.netty.buffer.ByteBufAllocator
import kotlin.jvm.Synchronized
import java.security.GeneralSecurityException
import java.lang.RuntimeException
import java.lang.Runnable
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import java.lang.IllegalArgumentException
import com.nukkitx.protocol.bedrock.util.EncryptionUtils
import javax.security.auth.DestroyFailedException
import com.nukkitx.protocol.bedrock.exception.PacketSerializeException
import io.netty.channel.EventLoop

class BedrockServerSession(connection: RakNetSession, eventLoop: EventLoop, serializer: BedrockWrapperSerializer) :
    BedrockSession(connection, eventLoop, serializer), MinecraftServerSession<BedrockPacket?> {
    override fun disconnect() {
        this.disconnect(null, true)
    }

    override fun disconnect(reason: String?) {
        this.disconnect(reason, false)
    }

    fun disconnect(reason: String?, hideReason: Boolean) {
        var reason = reason
        checkForClosed()
        val packet = DisconnectPacket()
        if (reason == null || hideReason) {
            packet.messageSkipped = true
            reason = "disconnect.disconnected"
        }
        packet.kickMessage = reason
        sendPacketImmediately(packet)
    }
}