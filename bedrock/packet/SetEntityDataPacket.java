package com.nukkitx.protocol.bedrock.packet;

import protocol.bedrock.BedrockPacket;
import protocol.bedrock.BedrockPacketType;
import com.nukkitx.protocol.bedrock.data.entity.EntityDataMap;
import com.nukkitx.protocol.bedrock.handler.BedrockPacketHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(doNotUseGetters = true, callSuper = false)
public class SetEntityDataPacket extends BedrockPacket {
    private final EntityDataMap metadata = new EntityDataMap();
    private long runtimeEntityId;
    private long tick;

    @Override
    public final boolean handle(BedrockPacketHandler handler) {
        return handler.handle(this);
    }

    public BedrockPacketType getPacketType() {
        return BedrockPacketType.SET_ENTITY_DATA;
    }
}
