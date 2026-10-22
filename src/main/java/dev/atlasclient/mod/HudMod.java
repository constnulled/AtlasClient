package dev.atlasclient.mod;

import net.minecraft.client.MinecraftClient;

import java.util.ArrayList;

/**
 * Manages the HUD (Heads-Up Display) settings.
 * <p>
 * This class contains settings for displaying various HUD elements.
 * </p>
 *
 * @author SpigotRCE
 * @since 1.10
 */
public class HudMod {
  /**
   * Indicates whether the server IP should be shown on the HUD.
   */
  public boolean showServerIP = true;

  /**
   * Indicates whether the tab list should be toggled or not.
   */
  public boolean showPlayerList = false;

  /**
   * List of HUD elements to be displayed.
   */
  public ArrayList<String> hudElements = new ArrayList<>();

  /**
   * Checks if the client is currently connected to a server.
   *
   * @param client The Minecraft client instance.
   *
   * @return True if connected to a server, false otherwise.
   */
  public boolean isConnectedToServer(MinecraftClient client) {
    return client.getNetworkHandler() != null && client.getCurrentServerEntry() != null;
  }

  /**
   * Retrieves the server status to be displayed on the HUD.
   *
   * @param client The Minecraft client instance.
   *
   * @return A string representing the server status.
   */
  public String getServerStatus(MinecraftClient client) {
    String address = client.getCurrentServerEntry().address;
    return showServerIP ? "Connected to: " + address : "Connected to: HIDDEN";
  }
}
