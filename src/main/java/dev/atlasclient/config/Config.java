package dev.atlasclient.config;

import dev.dejvokep.boostedyaml.YamlDocument;
import net.minecraft.client.MinecraftClient;
import dev.atlasclient.Constants;
import dev.atlasclient.themes.Theme;

import java.io.*;

public class Config extends ConfigProvider {
  private Theme theme = Theme.LEGACY;

  public Config() {
    super("config.yml", "config-version", new File(MinecraftClient.getInstance().runDirectory, "atlasclient"));
    try {
      load();
    } catch (IOException e) {
      Constants.LOGGER.error("Error loading config file", e);
    }
  }

  @Override public void reload() throws IOException {
    loadTheme();
  }

  private void loadTheme() {
    YamlDocument fileConfig = getFileConfig();
    String themeValue = fileConfig.getString("theme", "LEGACY");

    // Migrate legacy string values to enum
    switch (themeValue) {
      case "AtlasHack" -> theme = Theme.MATRIX;
      case "AtlasParticle" -> theme = Theme.PARTICLE;
      case "AtlasLegacy" -> theme = Theme.LEGACY;
      default -> {
        try {
          theme = Theme.valueOf(themeValue.toUpperCase());
        } catch (IllegalArgumentException e) {
          Constants.LOGGER.error("Invalid theme in config, defaulting to LEGACY", e);
          theme = Theme.LEGACY;
        }
      }
    }

    setTheme(theme);
  }

  public Theme getTheme() {
    return theme;
  }

  public void setTheme(Theme type) {
    this.theme = type;
    getFileConfig().set("theme", type.name());
    try {
      save();
    } catch (IOException e) {
      Constants.LOGGER.error("Error saving config file", e);
    }
  }
}
