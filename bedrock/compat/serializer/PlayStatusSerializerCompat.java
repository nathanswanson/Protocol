package com.nukkitx.protocol.bedrock.compat.serializer;

import protocol.bedrock.BedrockPacketHelper;
import protocol.bedrock.BedrockPacketSerializer;
import com.nukkitx.protocol.bedrock.packet.PlayStatusPacket;
import io.netty.buffer.ByteBuf;

public class PlayStatusSerializerCompat implements BedrockPacketSerializer<PlayStatusPacket> {
    public static final PlayStatusSerializerCompat INSTANCE = new PlayStatusSerializerCompat();

    @Override
    public void serialize(ByteBuf buffer, BedrockPacketHelper helper, PlayStatusPacket packet) {
        buffer.writeInt(packet.getStatus().ordinal());
    }

    @Override
    public void deserialize(ByteBuf buffer, BedrockPacketHelper helper, PlayStatusPacket packet) {
        packet.setStatus(PlayStatusPacket.Status.values()[buffer.readInt()]);
    }
}

