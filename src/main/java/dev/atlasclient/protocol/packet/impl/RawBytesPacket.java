package dev.atlasclient.protocol.packet.impl;

import io.netty.buffer.ByteBuf;
import dev.atlasclient.protocol.*;
import dev.atlasclient.protocol.packet.*;

/**
 * This packet allows to send raw bytes over any packet id
 *
 * @author SpigotRCE
 */
public class RawBytesPacket extends AbstractPacket {
  private final byte[] bytes;
  private final int id;

  public RawBytesPacket(byte[] bytes, int id) {
    this.bytes = bytes;
    this.id = id;
  }

  @Override public void write(ByteBuf b, Protocol protocol, ProtocolVersion.Direction direction, int protocolVersion) {
    b.writeBytes(bytes);
  }

  @Override public void handle(AbstractPacketHandler handler) throws Exception {
  }

  @Override public int hashCode() {
    return 0;
  }

  @Override public boolean equals(Object o) {
    return false;
  }

  @Override public String toString() {
    return "";
  }

  public byte[] getBytes() {
    return bytes;
  }

  public int getId() {
    return id;
  }
}
