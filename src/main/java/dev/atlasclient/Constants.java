package dev.atlasclient;

import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.*;

/**
 * Constants - Centralized configuration class for storing mod constants and metadata.
 * 
 * This class provides easy access to mod identification, version information,
 * logging, and other static configuration values used throughout the mod.
 */
public final class Constants {
    /** 
     * Unique identifier for the AtlasClient mod
     * Used for mod registration, configuration files, and resource locations
     */
    public static final String MOD_ID = "atlasclient";

    /** 
     * Human-readable name of the mod
     * Used for display purposes, window titles, and user-facing messages
     */
    public static final String MOD_NAME = "AtlasClient";

    /** 
     * Logger instance for the mod
     * Used for logging information, warnings, and errors with mod-specific context
     */
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

    /** 
     * Current version of the mod
     * Automatically retrieved from fabric.mod.json metadata at runtime
     */
    public static final String VERSION = FabricLoader.getInstance()
        .getModContainer(MOD_ID)
        .orElseThrow(() -> new IllegalStateException("Mod container not found for " + MOD_ID))
        .getMetadata()
        .getVersion()
        .getFriendlyString();

    /** 
     * Window title for the Minecraft game window
     * Combines mod name and version for easy identification
     */
    public static String windowTitle = MOD_NAME + " " + VERSION;
}