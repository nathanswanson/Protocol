package protocol.bedrock.packet;

import protocol.bedrock.BedrockPacket;
import protocol.bedrock.BedrockPacketType;
import protocol.bedrock.handler.BedrockPacketHandler;

@Data
@EqualsAndHashCode(doNotUseGetters = true, callSuper = false)
public class AddBehaviorTreePacket extends BedrockPacket {
    private String behaviorTreeJson;

    @Override
    public final boolean handle(BedrockPacketHandler handler) {
        return handler.handle(this);
    }

    public BedrockPacketType getPacketType() {
        return BedrockPacketType.ADD_BEHAVIOR_TREE;
    }
}
