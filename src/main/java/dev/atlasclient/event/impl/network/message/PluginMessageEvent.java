package dev.atlasclient.event.impl.network.message;

import net.minecraft.network.PacketByteBuf;

/**
 * Event for a plugin message received from the server.
 */
public record PluginMessageEvent(String channel, PacketByteBuf buf) {
  public PluginMessageEvent() {
    this(null, null);
  }
}
