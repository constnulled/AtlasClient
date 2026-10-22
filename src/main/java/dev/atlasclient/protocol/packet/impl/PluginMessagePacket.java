package dev.atlasclient.protocol.packet.impl;

import com.google.common.base.*;
import io.netty.buffer.ByteBuf;
import dev.atlasclient.protocol.ProtocolVersion;
import dev.atlasclient.protocol.packet.*;

import java.io.*;
import java.util.*;

public class PluginMessagePacket extends AbstractPacket {
  public static final Function<String, String> MODERNISE = tag -> {
    if (tag.equals("BungeeCord")) {
      return "bungeecord:main";
    } else if (tag.equals("bungeecord:main")) {
      return "BungeeCord";
    } else {
      return tag.indexOf(58) != -1 ? tag : "legacy:" + tag.toLowerCase(Locale.ROOT);
    }
  };
  private String tag;
  private byte[] data;
  private boolean allowExtendedPacket = false;

  public PluginMessagePacket() {
  }

  public PluginMessagePacket(String tag, byte[] data, boolean allowExtendedPacket) {
    this.tag = tag;
    this.data = data;
    this.allowExtendedPacket = allowExtendedPacket;
  }

  public void read(ByteBuf buf, ProtocolVersion.Direction direction, int protocolVersion) {
    this.tag = protocolVersion >= 393 ? MODERNISE.apply(readString(buf)) : readString(buf, 128);
    int maxSize = direction == ProtocolVersion.Direction.TO_SERVER ? 32767 : 1048576;
    Preconditions.checkArgument(buf.readableBytes() <= maxSize, "Payload too large");
    this.data = new byte[buf.readableBytes()];
    buf.readBytes(this.data);
  }

  public void write(ByteBuf buf, ProtocolVersion.Direction direction, int protocolVersion) {
    writeString(protocolVersion >= 393 ? MODERNISE.apply(this.tag) : this.tag, buf);
    buf.writeBytes(this.data);
  }

  @Override public void handle(AbstractPacketHandler handler) throws Exception {
    handler.handle(this);
  }

  public int hashCode() {
    int result = 1;
    result = result * 59 + (this.isAllowExtendedPacket() ? 79 : 97);
    Object $tag = this.getTag();
    result = result * 59 + ($tag == null ? 43 : $tag.hashCode());
    result = result * 59 + Arrays.hashCode(this.getData());
    return result;
  }

  public boolean equals(Object o) {
    if (o == this) {
      return true;
    } else if (!(o instanceof PluginMessagePacket other)) {
      return false;
    } else {
      if (!other.canEqual(this)) {
        return false;
      } else if (this.isAllowExtendedPacket() != other.isAllowExtendedPacket()) {
        return false;
      } else {
        Object this$tag = this.getTag();
        Object other$tag = other.getTag();
        if (this$tag == null) {
          if (other$tag != null) {
            return false;
          }
        } else if (!this$tag.equals(other$tag)) {
          return false;
        }

        return Arrays.equals(this.getData(), other.getData());
      }
    }
  }

  protected boolean canEqual(Object other) {
    return other instanceof PluginMessagePacket;
  }

  public String toString() {
    return "PluginMessage(tag=" +
      this.getTag() +
      ", data=" +
      Arrays.toString(this.getData()) +
      ", allowExtendedPacket=" +
      this.isAllowExtendedPacket() +
      ")";
  }

  public boolean isAllowExtendedPacket() {
    return this.allowExtendedPacket;
  }

  public String getTag() {
    return this.tag;
  }

  public void setTag(String tag) {
    this.tag = tag;
  }

  public byte[] getData() {
    return this.data;
  }

  public void setData(byte[] data) {
    this.data = data;
  }

  public void setAllowExtendedPacket(boolean allowExtendedPacket) {
    this.allowExtendedPacket = allowExtendedPacket;
  }

  public DataInput getStream() {
    return new DataInputStream(new ByteArrayInputStream(this.data));
  }
}
