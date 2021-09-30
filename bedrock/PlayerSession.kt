package protocol.bedrock

import network.common.util.DisconnectReason

interface PlayerSession {
    val isClosed: Boolean
    fun close()
    fun onDisconnect(reason: DisconnectReason?)
    fun onDisconnect(reason: String?)
}