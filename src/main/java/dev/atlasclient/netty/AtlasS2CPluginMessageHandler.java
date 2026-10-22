package dev.atlasclient.netty;

import io.netty.buffer.*;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import net.minecraft.network.PacketByteBuf;
import dev.atlasclient.*;
import dev.atlasclient.event.bus.EventBus;
import dev.atlasclient.event.impl.network.message.PluginMessageEvent;
import dev.atlasclient.protocol.*;
import dev.atlasclient.protocol.packet.AbstractPacket;
import dev.atlasclient.protocol.packet.impl.PluginMessagePacket;

import java.nio.charset.Charset;
import java.util.*;

public class AtlasS2CPluginMessageHandler extends MessageToMessageDecoder<ByteBuf> {
  @Override protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
    PacketByteBuf b = Helper.byteBufToPacketBuf(ctx.alloc().buffer().writeBytes(in));

    Protocol protocol = Helper.getBungeeProtocolForCurrentPhase();

    if (protocol != null) {
      int id = b.readVarInt();
      int protocolVersion = AtlasClient.NETWORK_CONFIGURATION.protocolVersion;

      AbstractPacket packet = protocol.TO_CLIENT.createPacket(id, protocolVersion);
      if (packet instanceof PluginMessagePacket message) {
        message.read(b.asByteBuf(), ProtocolVersion.Direction.TO_CLIENT, id);
        notifyChannels(message);

        EventBus.ListenerContext<PluginMessageEvent> context = EventBus.fire(EventBus.PLUGIN_MESSAGE_EVENT_CHANNEL,
          new PluginMessageEvent(message.getTag(), new PacketByteBuf(Unpooled.buffer().writeBytes(message.getData()))));

        if (context.isCancelled()) {
          return;
        }
      }
    }

    out.add(in.resetReaderIndex().retain());
  }

  private void notifyChannels(PluginMessagePacket message) {
    String channelName = message.getTag();
    PacketByteBuf buf = Helper.byteBufToPacketBuf(Unpooled.buffer().writeBytes(message.getData()));

    Helper.printChatMessage("&8&m-----------------------------------------------------", false);
    if (Objects.equals(channelName, "minecraft:register") || Objects.equals(channelName, "REGISTER")) {
      Helper.printChatMessage("&b&l[Channel Registration]");
      String[] channels = buf.toString(Charset.defaultCharset()).split("\000");

      for (String channel : channels) {
        boolean isKnown = AtlasClient.NETWORK_MOD.getRegisteredChannelsByName().contains(channel);
        String color = isKnown ? "&c" : "&a";
        Helper.printChatMessage("&7 - &fChannel: " + color + channel);

        if (isKnown) {
          Helper.showNotification("Exploit Detected!", "Channel: " + channel);
        }
      }
    } else {
      Helper.printChatMessage("&b&l[Channel Data]");
      String color = AtlasClient.NETWORK_MOD.getRegisteredChannelsByName().contains(channelName) ? "&c" : "&a";
      Helper.printChatMessage("&7 - &fChannel: " + color + channelName);
      Helper.printChatMessage("&7 - &fRaw Data: " + color + buf.toString(Charset.defaultCharset()));
    }

    Helper.printChatMessage("&8&m-----------------------------------------------------", false);
  }
}
