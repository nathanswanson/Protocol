package protocol.bedrock

import lombok.Value
import java.util.function.Supplier

@Value
class BedrockPacketDefinition<T : BedrockPacket?> {
    var id = 0
    var factory: Supplier<T>? = null
    var serializer: BedrockPacketSerializer<T>? = null
}