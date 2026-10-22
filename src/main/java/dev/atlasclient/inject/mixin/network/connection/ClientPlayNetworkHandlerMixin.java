package dev.atlasclient.inject.mixin.network.connection;

import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.encryption.NetworkEncryptionUtils;
import net.minecraft.network.message.*;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.World;
import dev.atlasclient.*;
import dev.atlasclient.event.bus.EventBus;
import dev.atlasclient.event.impl.chat.*;
import dev.atlasclient.inject.accessor.ClientPlayNetworkHandlerAccessor;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.time.Instant;

/**
 * Mixin class to modify the behavior of the ClientPlayNetworkHandler class.
 * <p>
 * This class handles the game join event and updates connection information.
 * </p>
 *
 * @author SpigotRCE
 * @since 2.17
 */
@Mixin(ClientPlayNetworkHandler.class)
public abstract class ClientPlayNetworkHandlerMixin implements ClientPlayNetworkHandlerAccessor {

  @Shadow private LastSeenMessagesCollector lastSeenMessagesCollector;

  @Shadow private MessageChain.Packer messagePacker;

  /**
   * Injects code at the end of the onGameJoin method to update connection status and server IP.
   * <p>
   * This method sets the connection status to true and updates the server IP address when the game join packet is
   * received.
   * </p>
   *
   * @param packet The game join packet received from the server.
   * @param info   The callback information.
   */
  @Inject(method = "onGameJoin", at = @At("TAIL")) private void onGameJoin(GameJoinS2CPacket packet,
    CallbackInfo info) {
    AtlasClient.NETWORK_MOD.isConnected = true;

    Helper.printChatMessage("&8&m-----------------------------------------------------", false);

    if (AtlasClient.MISC_MOD.isClientOutdated) {
      Helper.printChatMessage("&c&lWarning: &4Your client is outdated!");
      Helper.showNotification("Client is outdated!", "Latest: " + AtlasClient.MISC_MOD.latestVersion);
    }

    Helper.printChatMessage("");
    Helper.printChatMessage("&b&l[World Info]");
    Helper.printChatMessage("&7 - Dimension: &f" + packet.commonPlayerSpawnInfo().dimension().getValue());
    Helper.printChatMessage("&7 - Hashed Seed: &f" + packet.commonPlayerSpawnInfo().seed());

    Helper.printChatMessage("");
    Helper.printChatMessage("&b&l[Server Dimensions]");
    for (RegistryKey<World> dimension : packet.dimensionIds()) {
      Helper.printChatMessage("&7 - &f" + dimension.getValue());
    }

    Helper.printChatMessage("");
    Helper.printChatMessage("&b&l[Server Flags]");
    Helper.printChatMessage("&7 - Secure Chat: " + (packet.enforcesSecureChat() ? "&aEnabled" : "&cDisabled"));
    Helper.printChatMessage("&7 - Hardcore Mode: " + (packet.hardcore() ? "&aEnabled" : "&cDisabled"));
    Helper.printChatMessage("&7 - Max Players: &f" + packet.maxPlayers());
    Helper.printChatMessage("&7 - Render Distance: &f" + packet.viewDistance());
    Helper.printChatMessage("&7 - Simulation Distance: &f" + packet.simulationDistance());

    Helper.printChatMessage("&8&m-----------------------------------------------------", false);
  }

  /**
   * This method fires the {@link ChatPreEvent}.
   *
   * @param content The content entered by the player.
   * @param ci      The callback information.
   */
  @Inject(method = "sendChatMessage", at = @At("HEAD"), cancellable = true)
  private void onSendChatMessageH(String content, CallbackInfo ci) {

    EventBus.ListenerContext<ChatPreEvent> ctx = EventBus.CHAT_PRE_EVENT_CHANNEL.fire(new ChatPreEvent(content));

    if (ctx.isCancelled()) {
      ci.cancel();
      return;
    }

    content = ctx.getEvent().getMessage();

    if (content.startsWith(AtlasClient.COMMAND_MANAGER.prefix)) {
      AtlasClient.COMMAND_MANAGER.dispatch(content.substring(1));
      AtlasClient.MINECRAFT_CLIENT.inGameHud.getChatHud().addToMessageHistory(content);
      ci.cancel();
    }
  }

  /**
   * This method fires the {@link ChatPostEvent}.
   *
   * @param content The content entered by the player.
   * @param ci      The callback information.
   */
  @Inject(method = "sendChatMessage", at = @At("TAIL")) private void onSendChatMessageT(String content,
    CallbackInfo ci) {
    EventBus.fire(EventBus.CHAT_POST_EVENT_CHANNEL, new ChatPostEvent(content));
  }

  /**
   * Accessor method to send chat message internally without firing the chat events.
   *
   * @param message The message to be sent.
   */
  @Override public void atlasClient$sendChatMessage(String message) {
    Instant instant = Instant.now();
    long l = NetworkEncryptionUtils.SecureRandomUtil.nextLong();
    LastSeenMessagesCollector.LastSeenMessages lastSeenMessages = this.lastSeenMessagesCollector.collect();
    MessageSignatureData messageSignatureData =
      this.messagePacker.pack(new MessageBody(message, instant, l, lastSeenMessages.lastSeen()));
    Helper.sendPacket(new ChatMessageC2SPacket(message, instant, l, messageSignatureData, lastSeenMessages.update()));
  }
}
