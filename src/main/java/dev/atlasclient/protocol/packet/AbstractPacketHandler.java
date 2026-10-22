package dev.atlasclient.protocol.packet;

import dev.atlasclient.protocol.packet.impl.*;

public abstract class AbstractPacketHandler {
  public void handle(PluginMessagePacket packet) throws Exception {
    throw new IllegalAccessException("No handler for PluginMessagePacket in current phase!");
  }

  public void handle(HandshakePacket packet) throws Exception {
    throw new IllegalAccessException("No handler for HandshakePacket in current phase!");
  }
}
