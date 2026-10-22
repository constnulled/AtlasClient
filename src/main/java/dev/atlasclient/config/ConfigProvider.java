package dev.atlasclient.config;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;

import java.io.*;
import java.util.Objects;

/**
 * Abstract class representing a configuration provider. This class is responsible for loading, reloading, updating, and
 * saving configuration files.
 */
public abstract class ConfigProvider {
  /**
   * The name of the configuration file.
   */
  private final String fileName;
  /**
   * The versioning tag used for the configuration file.
   */
  private final String versioningTag;
  /**
   * The directory where the configuration file is stored.
   */
  private final File dataDirectory;
  /**
   * The YamlDocument representing the configuration file.
   */
  private YamlDocument fileConfig;

  /**
   * Constructs a new ConfigProvider with the specified fileName, versioningTag, and dataDirectory.
   *
   * @param fileName      The name of the configuration file.
   * @param versioningTag The versioning tag for the configuration file.
   * @param dataDirectory The directory where the configuration file is stored.
   */
  public ConfigProvider(String fileName, String versioningTag, File dataDirectory) {
    this.fileName = fileName;
    this.versioningTag = versioningTag;
    this.dataDirectory = dataDirectory;
  }

  /**
   * Constructs a new ConfigProvider with the specified fileName and versioningTag. The data directory is set to the
   * plugin's data folder.
   */
  public void load() throws IOException {
    this.fileConfig = YamlDocument.create(new File(getDataDirectory(), getFileName()),
      Objects.requireNonNull(getClass().getResourceAsStream("/" + getFileName())),
      GeneralSettings.DEFAULT,
      LoaderSettings.builder().setAutoUpdate(true).build(),
      DumperSettings.DEFAULT,
      UpdaterSettings.builder()
        .setVersioning(new BasicVersioning(getVersioningTag()))
        .setOptionSorting(UpdaterSettings.OptionSorting.SORT_BY_DEFAULTS)
        .build());

    update();
    save();
  }

  /**
   * Gets the directory where the configuration file is stored.
   *
   * @return The directory for the configuration file.
   */
  public File getDataDirectory() {
    return dataDirectory;
  }

  /**
   * Gets the name of the configuration file.
   *
   * @return The name of the configuration file.
   */
  public String getFileName() {
    return fileName;
  }

  /**
   * Gets the YamlDocument representing the configuration file.
   *
   * @return The YamlDocument for the configuration file.
   */
  public String getVersioningTag() {
    return versioningTag;
  }

  /**
   * Updates the configuration file. This method applies any changes made to the YamlDocument and saves it to the file.
   *
   * @throws IOException if an error occurs while updating the configuration file.
   */
  public void update() throws IOException {
    getFileConfig().update();
  }

  /**
   * Saves the configuration file. This method writes the current state of the YamlDocument to the file.
   *
   * @throws IOException if an error occurs while saving the configuration file.
   */
  public void save() throws IOException {
    getFileConfig().save();
  }

  /**
   * Gets the YamlDocument representing the configuration file.
   *
   * @return The YamlDocument for the configuration file.
   */
  public YamlDocument getFileConfig() {
    return fileConfig;
  }

  /**
   * Reloads the configuration file. This method reads the configuration file again and updates the YamlDocument.
   *
   * @throws IOException if an error occurs while reloading the configuration file.
   */
  public void reload() throws IOException {
    getFileConfig().reload();
    onReload();
  }

  /**
   * This method is called when the configuration is reloaded. It can be overridden by subclasses to perform additional
   * actions after reloading.
   */
  private void onReload() {
  }
}
