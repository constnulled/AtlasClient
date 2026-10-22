package dev.atlasclient;

/**
 * PacketFactory - Utility class for creating and sending various plugin messages
 * to proxy/server plugins through the AtlasClient mod.
 * 
 * This class provides static methods to send formatted packets to different
 * plugin channels for authentication, command execution, and other functionalities.
 */
public class PacketFactory {
    
    /**
     * Sends an AuthMeVelocity authentication packet to trigger login for a player.
     * Used for automatically logging in players through the AuthMeVelocity proxy plugin.
     *
     * @param name The Minecraft username to authenticate
     */
    public static void sendAMV(String name) {
        Helper.sendPluginMessage("authmevelocity:main", out -> {
            out.writeUTF("LOGIN");  // Packet type for login request
            out.writeUTF(name);     // Player username to authenticate
        });
    }

    /**
     * Sends an ECB (External Command Bridge) packet to execute console commands.
     * Used for remotely executing commands on the server through the ECB plugin.
     *
     * @param command The command to execute (without the leading slash)
     */
    public static void sendECB(String command) {
        Helper.sendPluginMessage("ecb:channel", out -> {
            out.writeUTF("ActionsSubChannel");      // ECB subchannel identifier
            out.writeUTF("console_command: " + command);  // Command to execute
        });
    }

    /**
     * Sends a SignedVelocity packet to modify command results.
     * Used for interacting with the SignedVelocity plugin to handle signed commands.
     *
     * @param uuid    The player's UUID as a string
     * @param command The command to modify (without the leading slash, but method adds it)
     */
    public static void sendSV(String uuid, String command) {
        Helper.sendPluginMessage("signedvelocity:main", out -> {
            out.writeUTF(uuid);         // Target player's UUID
            out.writeUTF("COMMAND_RESULT");  // Packet type for command result modification
            out.writeUTF("MODIFY");     // Action to perform on the command
            out.writeUTF("/" + command); // The command to modify (with leading slash)
        });
    }
}