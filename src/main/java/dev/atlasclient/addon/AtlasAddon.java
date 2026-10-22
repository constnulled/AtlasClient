package dev.atlasclient.addon;

/**
 * Base class for Atlas Client addons.
 */
public abstract class AtlasAddon {
  /**
   * Automatically assigned from fabric.mod.json file.
   */
  public String name;

  /**
   * Automatically assigned from fabric.mod.json file.
   */
  public String[] authors;

  public abstract void onInitialize();
}
