package com.nukkitx.protocol.bedrock.packet;

import protocol.bedrock.BedrockPacket;
import protocol.bedrock.BedrockPacketType;
import com.nukkitx.protocol.bedrock.data.EmoteFlag;
import com.nukkitx.protocol.bedrock.handler.BedrockPacketHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.EnumSet;
import java.util.Set;

@Data
@EqualsAndHashCode(doNotUseGetters = true, callSuper = false)
public class EmotePacket extends BedrockPacket {
    private long runtimeEntityId;
    private String emoteId;
    private final Set<EmoteFlag> flags = EnumSet.noneOf(EmoteFlag.class);

    @Override
    public boolean handle(BedrockPacketHandler handler) {
        return handler.handle(this);
    }

    public BedrockPacketType getPacketType() {
        return BedrockPacketType.EMOTE;
    }
}
