package protocol.bedrock

import network.raknet.RakNetPong
import java.nio.charset.StandardCharsets
import java.util.*

class BedrockPong (
    var edition: String? = null,
    var motd: String? = null,
    var protocolVersion: Int = -1,
    var version: String? = null,
    var playerCount: Int = -1,
    var maximumPlayerCount: Int = -1,
    var serverId: Long = 0,
    var subMotd: String? = null,
    var gameType: String? = null,
    var nintendoLimited: Boolean = false,
    var ipv4Port: Int = -1,
    var ipv6Port: Int = -1,
    var extras: Array<String?>? = null)
    {
    fun toRakNet(): ByteArray {
        val joiner = StringJoiner(";", "", ";")
            .add(edition)
            .add(toString(motd))
            .add(Integer.toString(protocolVersion))
            .add(toString(version))
            .add(Integer.toString(playerCount))
            .add(Integer.toString(maximumPlayerCount))
            .add(java.lang.Long.toString(serverId))
            .add(toString(subMotd))
            .add(toString(gameType))
            .add(if (nintendoLimited) "0" else "1")
            .add(Integer.toString(ipv4Port))
            .add(Integer.toString(ipv6Port))
        if (extras != null) {
            for (extra in extras!!) {
                joiner.add(extra)
            }
        }
        return joiner.toString().toByteArray(StandardCharsets.UTF_8)
    }

    companion object {
        fun fromRakNet(pong: RakNetPong): BedrockPong {
            val info = String(pong.userData, StandardCharsets.UTF_8)
            val bedrockPong = BedrockPong()
            val infos = info.split(";").toTypedArray()
            when (infos.size) {
                0 -> {
                }
                12 -> {
                    try {
                        bedrockPong.ipv6Port = infos[11].toInt()
                    } catch (e: NumberFormatException) {
                        // ignore
                    }
                    try {
                        bedrockPong.ipv4Port = infos[10].toInt()
                    } catch (e: NumberFormatException) {
                        // ignore
                    }
                    bedrockPong.nintendoLimited = !"1".equals(infos[9], ignoreCase = true)
                    bedrockPong.gameType = infos[8]
                    bedrockPong.subMotd = infos[7]
                    try {
                        bedrockPong.serverId = infos[6].toLong()
                    } catch (e: NumberFormatException) {
                        // ignore
                    }
                    try {
                        bedrockPong.maximumPlayerCount = infos[5].toInt()
                    } catch (e: NumberFormatException) {
                        // ignore
                    }
                    try {
                        bedrockPong.playerCount = infos[4].toInt()
                    } catch (e: NumberFormatException) {
                        // ignore
                    }
                    bedrockPong.version = infos[3]
                    try {
                        bedrockPong.protocolVersion = infos[2].toInt()
                    } catch (e: NumberFormatException) {
                        // ignore
                    }
                    bedrockPong.motd = infos[1]
                    bedrockPong.edition = infos[0]
                }
                11 -> {
                    try {
                        bedrockPong.ipv4Port = infos[10].toInt()
                    } catch (e: NumberFormatException) {
                    }
                    bedrockPong.nintendoLimited = !"1".equals(infos[9], ignoreCase = true)
                    bedrockPong.gameType = infos[8]
                    bedrockPong.subMotd = infos[7]
                    try {
                        bedrockPong.serverId = infos[6].toLong()
                    } catch (e: NumberFormatException) {
                    }
                    try {
                        bedrockPong.maximumPlayerCount = infos[5].toInt()
                    } catch (e: NumberFormatException) {
                    }
                    try {
                        bedrockPong.playerCount = infos[4].toInt()
                    } catch (e: NumberFormatException) {
                    }
                    bedrockPong.version = infos[3]
                    try {
                        bedrockPong.protocolVersion = infos[2].toInt()
                    } catch (e: NumberFormatException) {
                    }
                    bedrockPong.motd = infos[1]
                    bedrockPong.edition = infos[0]
                }
                10 -> {
                    bedrockPong.nintendoLimited = !"1".equals(infos[9], ignoreCase = true)
                    bedrockPong.gameType = infos[8]
                    bedrockPong.subMotd = infos[7]
                    try {
                        bedrockPong.serverId = infos[6].toLong()
                    } catch (e: NumberFormatException) {
                    }
                    try {
                        bedrockPong.maximumPlayerCount = infos[5].toInt()
                    } catch (e: NumberFormatException) {
                    }
                    try {
                        bedrockPong.playerCount = infos[4].toInt()
                    } catch (e: NumberFormatException) {
                    }
                    bedrockPong.version = infos[3]
                    try {
                        bedrockPong.protocolVersion = infos[2].toInt()
                    } catch (e: NumberFormatException) {
                    }
                    bedrockPong.motd = infos[1]
                    bedrockPong.edition = infos[0]
                }
                9 -> {
                    bedrockPong.gameType = infos[8]
                    bedrockPong.subMotd = infos[7]
                    try {
                        bedrockPong.serverId = infos[6].toLong()
                    } catch (e: NumberFormatException) {
                    }
                    try {
                        bedrockPong.maximumPlayerCount = infos[5].toInt()
                    } catch (e: NumberFormatException) {
                    }
                    try {
                        bedrockPong.playerCount = infos[4].toInt()
                    } catch (e: NumberFormatException) {
                    }
                    bedrockPong.version = infos[3]
                    try {
                        bedrockPong.protocolVersion = infos[2].toInt()
                    } catch (e: NumberFormatException) {
                    }
                    bedrockPong.motd = infos[1]
                    bedrockPong.edition = infos[0]
                }
                8 -> {
                    bedrockPong.subMotd = infos[7]
                    try {
                        bedrockPong.serverId = infos[6].toLong()
                    } catch (e: NumberFormatException) {
                    }
                    try {
                        bedrockPong.maximumPlayerCount = infos[5].toInt()
                    } catch (e: NumberFormatException) {
                    }
                    try {
                        bedrockPong.playerCount = infos[4].toInt()
                    } catch (e: NumberFormatException) {
                    }
                    bedrockPong.version = infos[3]
                    try {
                        bedrockPong.protocolVersion = infos[2].toInt()
                    } catch (e: NumberFormatException) {
                    }
                    bedrockPong.motd = infos[1]
                    bedrockPong.edition = infos[0]
                }
                7 -> {
                    try {
                        bedrockPong.serverId = infos[6].toLong()
                    } catch (e: NumberFormatException) {
                    }
                    try {
                        bedrockPong.maximumPlayerCount = infos[5].toInt()
                    } catch (e: NumberFormatException) {
                    }
                    try {
                        bedrockPong.playerCount = infos[4].toInt()
                    } catch (e: NumberFormatException) {
                    }
                    bedrockPong.version = infos[3]
                    try {
                        bedrockPong.protocolVersion = infos[2].toInt()
                    } catch (e: NumberFormatException) {
                    }
                    bedrockPong.motd = infos[1]
                    bedrockPong.edition = infos[0]
                }
                6 -> {
                    try {
                        bedrockPong.maximumPlayerCount = infos[5].toInt()
                    } catch (e: NumberFormatException) {
                    }
                    try {
                        bedrockPong.playerCount = infos[4].toInt()
                    } catch (e: NumberFormatException) {
                    }
                    bedrockPong.version = infos[3]
                    try {
                        bedrockPong.protocolVersion = infos[2].toInt()
                    } catch (e: NumberFormatException) {
                    }
                    bedrockPong.motd = infos[1]
                    bedrockPong.edition = infos[0]
                }
                5 -> {
                    try {
                        bedrockPong.playerCount = infos[4].toInt()
                    } catch (e: NumberFormatException) {
                    }
                    bedrockPong.version = infos[3]
                    try {
                        bedrockPong.protocolVersion = infos[2].toInt()
                    } catch (e: NumberFormatException) {
                    }
                    bedrockPong.motd = infos[1]
                    bedrockPong.edition = infos[0]
                }
                4 -> {
                    bedrockPong.version = infos[3]
                    try {
                        bedrockPong.protocolVersion = infos[2].toInt()
                    } catch (e: NumberFormatException) {
                    }
                    bedrockPong.motd = infos[1]
                    bedrockPong.edition = infos[0]
                }
                3 -> {
                    try {
                        bedrockPong.protocolVersion = infos[2].toInt()
                    } catch (e: NumberFormatException) {
                    }
                    bedrockPong.motd = infos[1]
                    bedrockPong.edition = infos[0]
                }
                2 -> {
                    bedrockPong.motd = infos[1]
                    bedrockPong.edition = infos[0]
                }
                1 -> bedrockPong.edition = infos[0]
                else -> {
                    bedrockPong.extras = arrayOfNulls(infos.size - 12)
                    System.arraycopy(infos, 12, bedrockPong.extras, 0, bedrockPong.extras!!.size)
                    try {
                        bedrockPong.ipv6Port = infos[11].toInt()
                    } catch (e: NumberFormatException) {
                    }
                    try {
                        bedrockPong.ipv4Port = infos[10].toInt()
                    } catch (e: NumberFormatException) {
                    }
                    bedrockPong.nintendoLimited = !"1".equals(infos[9], ignoreCase = true)
                    bedrockPong.gameType = infos[8]
                    bedrockPong.subMotd = infos[7]
                    try {
                        bedrockPong.serverId = infos[6].toLong()
                    } catch (e: NumberFormatException) {
                    }
                    try {
                        bedrockPong.maximumPlayerCount = infos[5].toInt()
                    } catch (e: NumberFormatException) {
                    }
                    try {
                        bedrockPong.playerCount = infos[4].toInt()
                    } catch (e: NumberFormatException) {
                    }
                    bedrockPong.version = infos[3]
                    try {
                        bedrockPong.protocolVersion = infos[2].toInt()
                    } catch (e: NumberFormatException) {
                    }
                    bedrockPong.motd = infos[1]
                    bedrockPong.edition = infos[0]
                }
            }
            return bedrockPong
        }

        private fun toString(string: String?): String {
            return string ?: ""
        }
    }
}