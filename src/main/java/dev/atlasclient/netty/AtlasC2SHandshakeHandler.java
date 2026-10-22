package dev.atlasclient.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import net.minecraft.network.PacketByteBuf;
import dev.atlasclient.*;
import dev.atlasclient.protocol.ProtocolVersion;
import dev.atlasclient.protocol.packet.impl.HandshakePacket;

import java.util.List;

public class AtlasC2SHandshakeHandler extends MessageToMessageEncoder<ByteBuf> {
  @Override protected void encode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
    PacketByteBuf b = Helper.byteBufToPacketBuf(ctx.alloc().buffer().writeBytes(in));

    b.readVarInt();
    decodeHandshake(b);

    ctx.pipeline().remove(this);

    out.add(in.resetReaderIndex().retain());
  }

  private void decodeHandshake(PacketByteBuf b) {
    HandshakePacket handshake = new HandshakePacket();
    handshake.read(b.asByteBuf());
    int protocolVersion = handshake.getProtocolVersion();
    if (!(ProtocolVersion.isSupported(protocolVersion))) {
      throw new IllegalArgumentException("Protocol version: " +
        protocolVersion +
        " is not supported by AtlasClient!");
    }
    AtlasClient.NETWORK_CONFIGURATION.protocolVersion = protocolVersion;
  }
}
