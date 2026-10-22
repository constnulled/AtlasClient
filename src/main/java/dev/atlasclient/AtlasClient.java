package dev.atlasclient;

import net.fabricmc.api.*;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import dev.atlasclient.addon.AddonLoader;
import dev.atlasclient.command.CommandManager;
import dev.atlasclient.ui.screen.ClickGuiScreen;
import dev.atlasclient.config.Config;
import dev.atlasclient.exploit.ExploitManager;
import dev.atlasclient.mod.*;
import dev.atlasclient.packet.DummyPacket;
import dev.atlasclient.ui.notification.NotificationManager;
import org.lwjgl.glfw.*;
import org.lwjgl.system.MemoryStack;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.ByteBuffer;
import java.util.Objects;

/**
 * The main class for the AtlasClient Fabric mod.
 * <p>
 * This class implements the {@link ModInitializer} interface and is responsible for initializing the various components
 * and modules of the mod when the mod is loaded.
 * </p>
 *
 * @author SpigotRCE
 */
public class AtlasClient implements ModInitializer, ClientModInitializer {
  // Core Minecraft client instance
  public static final MinecraftClient MINECRAFT_CLIENT = MinecraftClient.getInstance();
  
  // Network configuration for tracking connection state
  public static final NetworkConfiguration NETWORK_CONFIGURATION = new NetworkConfiguration();
  
  // Singleton instance of the main mod class
  public static AtlasClient INSTANCE;
  
  // Mod modules - each handles specific functionality
  public static BungeeSpoofMod BUNGEE_SPOOF_MOD;
  public static MiscMod MISC_MOD;
  public static HudMod HUD_MOD;
  public static ExploitMod EXPLOIT_MOD;
  
  // Manager classes for handling commands, exploits, and other features
  public static CommandManager COMMAND_MANAGER;
  public static ExploitManager EXPLOIT_MANAGER;
  public static NetworkMod NETWORK_MOD;
  public static NotificationManager NOTIFICATION_MANAGER;
  
  // Configuration handler for mod settings
  public static Config CONFIG;

  /**
   * Initializes the client-side components of the mod.
   * Called when Minecraft client starts up.
   */
  @Override 
  public void onInitializeClient() {
    
    // Initialize core mod components
    INSTANCE = this;
    updateIcon();
    CONFIG = new Config();
    
    // Load configuration from file
    try {
      CONFIG.load();
    } catch (IOException e) {
      throw new RuntimeException("Failed to load configuration", e);
    }
    
    // Set up various mod systems
    registerChannels();
    initializeMods();
    initializeManagers();
    setupKeyBindings();
    checkForUpdates();
    AddonLoader.loadAddons();
  }

  /**
   * Updates the Minecraft window icon with custom AtlasClient icons.
   * Uses GLFW to set window icons of different sizes.
   */
  private void updateIcon() {
    MINECRAFT_CLIENT.execute(() -> {
      try (MemoryStack stack = MemoryStack.stackPush()) {
        long windowHandle = MINECRAFT_CLIENT.getWindow().getHandle();

        // Allocate buffer for multiple icon sizes
        GLFWImage.Buffer icons = GLFWImage.malloc(2, stack);

        // Load and set 16x16 and 32x32 icons
        icons.put(0, loadIcon("/assets/atlasclient/textures/icon/icon_16.png", stack));
        icons.put(1, loadIcon("/assets/atlasclient/textures/icon/icon_32.png", stack));

        // Apply the icons to the window
        GLFW.glfwSetWindowIcon(windowHandle, icons);
      } catch (Exception e) {
        Constants.LOGGER.error("Failed to set window icon", e);
      }
    });
  }

  /**
   * Registers custom plugin message channels for communication with server plugins.
   * These channels enable interaction with various proxy and server-side plugins.
   */
  private void registerChannels() {
    registerChannel("velocityreport:main");
    registerChannel("purpur:beehive_c2s");
    registerChannel("authmevelocity:main");
    registerChannel("chatsentry:data_sync");
    registerChannel("ecb:channel");
    registerChannel("signedvelocity:main");
  }

  /**
   * Initializes all mod modules with their specific functionalities.
   */
  private void initializeMods() {
    BUNGEE_SPOOF_MOD = new BungeeSpoofMod();
    MISC_MOD = new MiscMod();
    HUD_MOD = new HudMod();
    EXPLOIT_MOD = new ExploitMod();
    NETWORK_MOD = new NetworkMod();
  }

  /**
   * Initializes manager classes that handle various aspects of the mod.
   */
  private void initializeManagers() {
    EXPLOIT_MANAGER = new ExploitManager(MINECRAFT_CLIENT);
    EXPLOIT_MANAGER.init();

    COMMAND_MANAGER = new CommandManager(MINECRAFT_CLIENT);
    COMMAND_MANAGER.init();

    NOTIFICATION_MANAGER = new NotificationManager();
  }
  
  private void setupKeyBindings() {
    KeyBinding keyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
      "Open atlas command",
      InputUtil.Type.KEYSYM,
      GLFW.GLFW_KEY_COMMA,
      Constants.MOD_NAME
    ));

    KeyBinding clickGuiKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("Open Paradise Click GUI",
      InputUtil.Type.KEYSYM,
      GLFW.GLFW_KEY_RIGHT_SHIFT,
      Constants.MOD_NAME));
    
    ClientTickEvents.END_CLIENT_TICK.register(client -> {
      while (keyBinding.wasPressed()) {
        client.setScreen(new ChatScreen(COMMAND_MANAGER.prefix));
      }
      while (clickGuiKey.wasPressed()) {
        client.setScreen(new ClickGuiScreen());
      }
    });
  }

  private void checkForUpdates() {
    new Thread(() -> {
      try {
        String latestVersion = Helper.getLatestReleaseTag();
        if (latestVersion == null) {
          return;  // API request failed, skip update check
        }

        // Store version info and check if client is outdated
        MISC_MOD.latestVersion = latestVersion;
        MISC_MOD.isClientOutdated = !Objects.equals(latestVersion, Constants.VERSION);
        
        // Notify user if update is available
        if (MISC_MOD.isClientOutdated) {
          try {
            Helper.showTrayMessage(
              "AtlasClient is Best! Latest version: " + latestVersion,
              TrayIcon.MessageType.WARNING
            );
          } catch (AWTException e) {
            Constants.LOGGER.error("Failed to show tray message for update", e);
          }
        }

      } catch (IOException e) {
        Constants.LOGGER.error("Error checking for latest release tag", e);
      }
    }).start();
  }

  /**
   * Loads an icon image from resources and converts it to GLFW format.
   *
   * @param path The resource path to the icon image
   * @param stack The memory stack for buffer allocation
   * @return GLFWImage containing the loaded icon
   * @throws IOException If the icon cannot be loaded from resources
   */
  private GLFWImage loadIcon(String path, MemoryStack stack) throws IOException {
    InputStream input = getClass().getResourceAsStream(path);
    if (input == null) {
      throw new IOException("Icon not found: " + path);
    }

    // Read image and extract pixel data
    BufferedImage image = ImageIO.read(input);
    int width = image.getWidth();
    int height = image.getHeight();
    int[] pixelsRaw = new int[width * height];
    image.getRGB(0, 0, width, height, pixelsRaw, 0, width);

    // Convert to RGBA ByteBuffer format required by GLFW
    ByteBuffer pixels = stack.malloc(width * height * 4);

    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        int pixel = pixelsRaw[y * width + x];
        pixels.put((byte) ((pixel >> 16) & 0xFF)); // Red
        pixels.put((byte) ((pixel >> 8) & 0xFF));  // Green
        pixels.put((byte) (pixel & 0xFF));         // Blue
        pixels.put((byte) ((pixel >> 24) & 0xFF)); // Alpha
      }
    }
    pixels.flip();

    // Create GLFW image structure
    GLFWImage icon = GLFWImage.malloc(stack);
    icon.set(width, height, pixels);
    return icon;
  }

  /**
   * Registers a custom plugin channel with the network system.
   *
   * @param channelName The channel name in "namespace:id" format
   */
  public void registerChannel(String channelName) {
    String nameSpace = channelName.split(":")[0];
    String id = channelName.split(":")[1];
    PayloadTypeRegistry.playC2S().register(
      new CustomPayload.Id<>(Identifier.of(nameSpace, id)), 
      DummyPacket.CODEC
    );
  }

  /**
   * Server-side initialization method (unused for client mod).
   */
  @Override 
  public void onInitialize() {
    // Empty implementation - this is a client-side mod
  }
}
