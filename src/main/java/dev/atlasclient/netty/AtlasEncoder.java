package dev.atlasclient.netty;

import io.netty.buffer.*;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import dev.atlasclient.*;
import dev.atlasclient.protocol.Protocol;
import dev.atlasclient.protocol.packet.AbstractPacket;
import dev.atlasclient.protocol.packet.impl.RawBytesPacket;

public class AtlasEncoder extends MessageToByteEncoder<AbstractPacket> {
  protected void encode(ChannelHandlerContext ctx, AbstractPacket msg, ByteBuf out) throws Exception {
    if (msg.isEncoded() && msg.getEncodedBuf() != null) {
      ByteBuf cached = msg.getEncodedBuf().duplicate().resetReaderIndex();
      out.writeBytes(cached);
      return;
    }

    Protocol protocol = Helper.getBungeeProtocolForCurrentPhase();
    int protocolVersion = AtlasClient.NETWORK_CONFIGURATION.protocolVersion;

    int packetId = (msg instanceof RawBytesPacket packet) ?
      packet.getId() :
      protocol.TO_SERVER.getId(msg.getClass(), protocolVersion);

    int startIndex = out.writerIndex();
    AbstractPacket.writeVarInt(packetId, out);
    msg.write(out, protocol, protocol.TO_SERVER.getDirection(), protocolVersion);
    ByteBuf encoded = out.slice(startIndex, out.writerIndex() - startIndex).retain();
    msg.setEncoded(true, encoded);
  }
}
