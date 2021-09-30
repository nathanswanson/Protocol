package com.nukkitx.protocol.bedrock.compat.serializer;

import network.common.VarInts;
import protocol.bedrock.BedrockPacketHelper;
import protocol.bedrock.BedrockPacketSerializer;
import com.nukkitx.protocol.bedrock.packet.LoginPacket;
import io.netty.buffer.ByteBuf;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class LoginSerializerCompat implements BedrockPacketSerializer<LoginPacket> {
    public static final LoginSerializerCompat INSTANCE = new LoginSerializerCompat();

    @Override
    public void serialize(ByteBuf buffer, BedrockPacketHelper helper, LoginPacket packet) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deserialize(ByteBuf buffer, BedrockPacketHelper helper, LoginPacket packet) {
        packet.setProtocolVersion(buffer.readInt());

        ByteBuf jwt = buffer.readSlice(VarInts.readUnsignedInt(buffer)); // Get the JWT.
        packet.setChainData(helper.readLEAsciiString(jwt));
        packet.setSkinData(helper.readLEAsciiString(jwt));
    }
}
