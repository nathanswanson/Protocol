package com.nukkitx.protocol.bedrock.packet;

import protocol.bedrock.BedrockPacket;
import protocol.bedrock.BedrockPacketType;
import com.nukkitx.protocol.bedrock.handler.BedrockPacketHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(doNotUseGetters = true, callSuper = false)
public class ShowCreditsPacket extends BedrockPacket {
    private long runtimeEntityId;
    private Status status;

    @Override
    public final boolean handle(BedrockPacketHandler handler) {
        return handler.handle(this);
    }

    public BedrockPacketType getPacketType() {
        return BedrockPacketType.SHOW_CREDITS;
    }

    public enum Status {
        START_CREDITS,
        END_CREDITS
    }
}
