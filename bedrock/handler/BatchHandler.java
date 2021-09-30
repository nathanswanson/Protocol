package com.nukkitx.protocol.bedrock.handler;

import protocol.bedrock.BedrockPacket;
import protocol.bedrock.BedrockSession;
import io.netty.buffer.ByteBuf;

import java.util.Collection;

public interface BatchHandler {

    void handle(BedrockSession session, ByteBuf compressed, Collection<BedrockPacket> packets);
}
