package dev.atlasclient.addon;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.metadata.Person;
import dev.atlasclient.Constants;

import java.util.*;

/**
 * Handles the loading and management of Atlas addons.
 * This class is responsible for discovering, initializing, and tracking all loaded addons.
 */
public class AddonLoader {
  // List to store all successfully loaded addons
  private static final List<AtlasAddon> loadedAddons = new ArrayList<>();

  /**
   * Loads all available addons from the Fabric mod container.
   * This method discovers addons with the "atlas" entrypoint, initializes them,
   * and adds them to the loaded addons list.
   */
  public static void loadAddons() {
    // Get all entrypoint containers for the "atlas" entrypoint type and process each one
    FabricLoader.getInstance().getEntrypointContainers("atlas", AtlasAddon.class).forEach(container -> {
      try {
        // Get the addon instance from the container
        AtlasAddon addon = container.getEntrypoint();
        
        // Set the addon name from the mod metadata
        addon.name = container.getProvider().getMetadata().getName();
        
        // Extract author names from the mod metadata and convert to string array
        addon.authors = container.getProvider().getMetadata().getAuthors()
          .stream()
          .map(Person::getName)
          .toArray(String[]::new);
        
        // Add the addon to our loaded addons list
        loadedAddons.add(addon);
        
        // Initialize the addon by calling its onInitialize method
        addon.onInitialize();
        
        // Log successful addon loading
        Constants.LOGGER.info("Loaded addon: {}", addon.name);
      } catch (Throwable e) {
        // Log any errors that occur during addon loading
        Constants.LOGGER.error("Failed to load addon: {}", 
          container.getProvider().getMetadata().getName(), e);
      }
    });
  }

  /**
   * Returns an unmodifiable list of all currently loaded addons.
   * 
   * @return List of loaded AtlasAddon instances
   */
  public static List<AtlasAddon> getLoadedAddons() {
    return loadedAddons;
  }
}