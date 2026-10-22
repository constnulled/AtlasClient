package dev.atlasclient;

import com.google.common.io.*;
import com.google.gson.*;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.*;
import net.minecraft.network.packet.Packet;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import dev.atlasclient.inject.accessor.ClientConnectionAccessor;
import dev.atlasclient.protocol.Protocol;
import dev.atlasclient.protocol.packet.AbstractPacket;
import dev.atlasclient.protocol.packet.impl.*;
import dev.atlasclient.ui.notification.Notification;

import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.List;
import java.util.*;

/**
 * Utility class providing various helper methods for Minecraft client operations.
 * <p>
 * This class includes methods for generating chroma colors, sending chat messages, parsing and formatting colored text,
 * and more.
 * </p>
 *
 * @author SpigotRCE
 * @since 1.0
 */
public class Helper {
  /**
   * Sends a chat message to the Minecraft player.
   *
   * @param message The message to be sent.
   */
  public static void printChatMessage(String message) {
    printChatMessage(message, true);
  }

  /**
   * Sends a chat message to the Minecraft player with optional prefix.
   *
   * @param message The message to be sent.
   * @param dropTitle Whether to prepend the client prefix to the message
   */
  public static void printChatMessage(String message, boolean dropTitle) {
    printChatMessage(Text.of(parseColoredText(dropTitle ? appendPrefix(message) : message)));
  }

  /**
   * Queues a text message for display in the chat.
   * Uses delayed message system to ensure proper timing.
   *
   * @param message The text message to display
   */
  public static void printChatMessage(Text message) {
    AtlasClient.MISC_MOD.delayedMessages.add(message);
  }

  /**
   * Appends the client prefix to a message string.
   *
   * @param text The original message text
   * @return The message with client prefix prepended
   */
  public static String appendPrefix(String text) {
    return "&aAtlas&bClient &r" + text;
  }

  /**
   * Parses a string message into a colored {@link Text} object.
   *
   * @param message The message to be parsed.
   * @return The formatted {@link Text} object.
   */
  public static Text parseColoredText(String message) {
    return parseColoredText(message, null);
  }

  /**
   * Parses a string message into a colored {@link Text} object with an optional click-to-copy action.
   *
   * @param message     The message to be parsed.
   * @param copyMessage The message to copy to the clipboard when clicked, or {@code null} for no action.
   * @return The formatted {@link Text} object.
   */
  public static Text parseColoredText(String message, String copyMessage) {
    MutableText text = Text.literal("");
    String[] parts = message.split("(?=&)");
    List<Formatting> currentFormats = new ArrayList<>();

    for (String part : parts) {
      if (part.isEmpty()) {
        continue;
      }
      if (part.startsWith("&")) {
        // Extract color code and apply formatting
        currentFormats.add(getColorFromCode(part.substring(0, 2)));
        String remaining = part.substring(2);
        if (!remaining.isEmpty()) {
          MutableText formattedText = Text.literal(remaining);
          for (Formatting format : currentFormats) {
            formattedText = formattedText.formatted(format);
          }
          text.append(formattedText);
        }
      } else {
        // Apply current formatting stack to non-color code text
        MutableText unformattedText = Text.literal(part);
        for (Formatting format : currentFormats) {
          unformattedText = unformattedText.formatted(format);
        }
        text.append(unformattedText);
      }
    }

    // Add click-to-copy functionality if specified
    if (copyMessage != null && !copyMessage.isEmpty()) {
      text.setStyle(text.getStyle().withClickEvent(new ClickEvent.CopyToClipboard(copyMessage)));
    }

    return text;
  }

  /**
   * Converts a color code string to a {@link Formatting} enum value.
   *
   * @param code The color code string (e.g., "&0", "&1").
   * @return The corresponding {@link Formatting} value.
   */
  private static Formatting getColorFromCode(String code) {
    return switch (code) {
      case "&0" -> Formatting.BLACK;
      case "&1" -> Formatting.DARK_BLUE;
      case "&2" -> Formatting.DARK_GREEN;
      case "&3" -> Formatting.DARK_AQUA;
      case "&4" -> Formatting.DARK_RED;
      case "&5" -> Formatting.DARK_PURPLE;
      case "&6" -> Formatting.GOLD;
      case "&7" -> Formatting.GRAY;
      case "&8" -> Formatting.DARK_GRAY;
      case "&9" -> Formatting.BLUE;
      case "&a" -> Formatting.GREEN;
      case "&b" -> Formatting.AQUA;
      case "&c" -> Formatting.RED;
      case "&d" -> Formatting.LIGHT_PURPLE;
      case "&e" -> Formatting.YELLOW;
      case "&f" -> Formatting.WHITE;
      case "&k" -> Formatting.OBFUSCATED;
      case "&l" -> Formatting.BOLD;
      case "&m" -> Formatting.STRIKETHROUGH;
      case "&n" -> Formatting.UNDERLINE;
      case "&o" -> Formatting.ITALIC;
      default -> Formatting.RESET;
    };
  }

  /**
   * Checks if a string can be parsed as a number.
   *
   * @param s The string to check.
   * @return {@code true} if the string is a valid number, {@code false} otherwise.
   */
  @SuppressWarnings("unused") 
  public static boolean isNumber(String s) {
    try {
      Double.parseDouble(s);
      return true;
    } catch (NumberFormatException e) {
      return false;
    }
  }

  /**
   * Sends a network packet to the Minecraft server.
   *
   * @param packet The packet to be sent.
   */
  public static void sendPacket(Packet<?> packet) {
    Objects.requireNonNull(MinecraftClient.getInstance().getNetworkHandler()).sendPacket(packet);
  }

  /**
   * Sends a plugin message packet to the specified channel.
   *
   * @param channel The plugin message channel identifier
   * @param encoder The encoder function that writes data to the output stream
   */
  public static void sendPluginMessage(String channel, PluginMessagePacketEncoder encoder) {
    ByteArrayDataOutput out = ByteStreams.newDataOutput();
    encoder.encode(out);
    PluginMessagePacket message = new PluginMessagePacket();
    message.setTag(channel);
    message.setData(out.toByteArray());
    sendPacket(message);
  }

  /**
   * Sends a custom abstract packet to the server.
   * Uses direct channel access for custom packet types.
   *
   * @param packet The abstract packet to send
   */
  public static void sendPacket(AbstractPacket packet) {
    ((ClientConnectionAccessor) MinecraftClient.getInstance()
      .getNetworkHandler()
      .getConnection()).atlasClient$getChannel().write(packet);
  }

  /**
   * Gets the BungeeCord protocol for the current network phase.
   *
   * @return The protocol corresponding to current network phase
   */
  public static Protocol getBungeeProtocolForCurrentPhase() {
    return getBungeeProtocolForPhase(AtlasClient.NETWORK_CONFIGURATION.phase);
  }

  /**
   * Gets the BungeeCord protocol for a specific network phase.
   *
   * @param phase The network phase to get protocol for
   * @return The protocol corresponding to the network phase
   */
  public static Protocol getBungeeProtocolForPhase(NetworkPhase phase) {
    switch (phase) {
      case HANDSHAKING -> {
        return Protocol.HANDSHAKE;
      }
      case PLAY -> {
        return Protocol.GAME;
      }
      case STATUS -> {
        return Protocol.STATUS;
      }
      case LOGIN -> {
        return Protocol.LOGIN;
      }
      case CONFIGURATION -> {
        return Protocol.CONFIGURATION;
      }
      default -> throw new IllegalArgumentException("Unknown protocol state: " + phase.getId());
    }
  }

  /**
   * Capitalizes the first letter of a string.
   *
   * @param str The string to capitalize.
   * @return The string with the first letter capitalized, or the original string if it is null or empty.
   */
  public static String capitalizeFirstLetter(String str) {
    if (str == null || str.isEmpty()) {
      return str;
    }
    return str.substring(0, 1).toUpperCase() + str.substring(1);
  }

  /**
   * Generates a random string.
   *
   * @param length     The length of the created string.
   * @param characters The charset the generator will use.
   * @param random     The {@link Random} instance the generator will use.
   * @return The random string generated.
   */
  public static String generateRandomString(int length, String characters, Random random) {
    StringBuilder result = new StringBuilder(length);
    for (int i = 0; i < length; i++) {
      result.append(characters.charAt(random.nextInt(characters.length())));
    }
    return result.toString();
  }

  /**
   * Fetches the latest release tag from the AtlasClient API.
   *
   * @return The latest version tag, or null if the request fails
   * @throws IOException If there's an error connecting to the API
   */
  public static String getLatestReleaseTag() throws IOException {
    HttpURLConnection connection =
      (HttpURLConnection) new URL("http://paradise-client.net/api/versions").openConnection();
    connection.setRequestProperty("Accept", "application/json");
    connection.setRequestMethod("GET");

    if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
      BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
      StringBuilder response = new StringBuilder();
      String line;
      while ((line = reader.readLine()) != null) {
        response.append(line);
      }
      reader.close();

      JsonObject responseObject = JsonParser.parseString(response.toString()).getAsJsonObject();
      return responseObject.getAsJsonObject("latest_version").get("version").getAsString();
    } else {
      return null;
    }
  }

  /**
   * Displays a notification in the client UI.
   *
   * @param title The notification title
   * @param message The notification message
   */
  public static void showNotification(String title, String message) {
    AtlasClient.NOTIFICATION_MANAGER.addNotification(new Notification(title, message));
  }

  /**
   * Displays a system tray message with default title and info type.
   *
   * @param message The message to display
   * @throws AWTException If the system tray is not supported
   */
  public static void showTrayMessage(String message) throws AWTException {
    showTrayMessage("AtlasClient", message, TrayIcon.MessageType.INFO);
  }

  /**
   * Displays a system tray message with custom parameters.
   *
   * @param title The tray message title
   * @param message The tray message content
   * @param messageType The type of message (INFO, WARNING, ERROR)
   * @throws AWTException If the system tray is not supported
   */
  public static void showTrayMessage(String title, String message, TrayIcon.MessageType messageType)
    throws AWTException {
    if (!SystemTray.isSupported()) {
      throw new UnsupportedOperationException();
    }

    SystemTray tray = SystemTray.getSystemTray();
    // I don't know why the image isn't working,
    // I have tried everything I could think of
    Image image = Toolkit.getDefaultToolkit().createImage("assets/atlasclient/textures/icon/icon.png");
    TrayIcon icon = new TrayIcon(image, "AtlasClient");
    icon.setImageAutoSize(true);
    tray.add(icon);
    icon.displayMessage(title, message, messageType);
  }

  /**
   * Displays a system tray message with custom message type.
   *
   * @param message The message to display
   * @param messageType The type of message (INFO, WARNING, ERROR)
   * @throws AWTException If the system tray is not supported
   */
  public static void showTrayMessage(String message, TrayIcon.MessageType messageType) throws AWTException {
    showTrayMessage("AtlasClient", message, messageType);
  }

  /**
   * Converts a Netty ByteBuf to a Minecraft PacketByteBuf.
   *
   * @param buf The Netty ByteBuf to convert
   * @return The converted PacketByteBuf
   */
  public static PacketByteBuf byteBufToPacketBuf(ByteBuf buf) {
    return new PacketByteBuf(buf);
  }

  /**
   * Fetches the UUID for a Minecraft username using Mojang's API.
   *
   * @param username The Minecraft username to lookup
   * @return The UUID as a string
   * @throws Exception If the API request fails or username is not found
   */
  public static UUID fetchUUID(String username) throws Exception {
    URL url = new URL("https://api.minecraftservices.com/minecraft/profile/lookup/name/" + username);
    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
    connection.setRequestMethod("GET");
    if (connection.getResponseCode() == 200) {
      try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
        String response = reader.lines().reduce("", (acc, line) -> acc + line);
        return UUID.fromString(JsonParser.parseString(response)
          .getAsJsonObject()
          .get("id")
          .getAsString()
          .replaceFirst("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5"));
      }
    }
    throw new Exception("Failed to fetch UUID");
  }

  /**
   * Runs a task asynchronously in a new thread.
   *
   * @param runnable The task to run
   */
  public static void runAsync(Runnable runnable) {
    new Thread(runnable).start();
  }

  /**
   * Functional interface for encoding plugin message packets.
   */
  @FunctionalInterface 
  public static interface PluginMessagePacketEncoder {
    void encode(ByteArrayDataOutput out);
  }

  /**
   * Wrapper class for ByteArrayDataOutput with utility methods.
   */
  @SuppressWarnings("unused") 
  public static class ByteArrayOutput {
    private final ByteArrayDataOutput out;

    public ByteArrayOutput() {
      this.out = ByteStreams.newDataOutput();
    }

    public ByteArrayOutput(byte[] bytes) {
      this.out = ByteStreams.newDataOutput();
      out.write(bytes);
    }

    public ByteArrayDataOutput getBuf() {
      return out;
    }

    public byte[] toByteArray() {
      return out.toByteArray();
    }
  }
}
