package dev.atlasclient.command.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.network.packet.c2s.play.RequestCommandCompletionsC2SPacket;
import dev.atlasclient.*;
import dev.atlasclient.command.Command;
import dev.atlasclient.command.CommandManager;


import java.util.Random;

/**
 * DumpCommand - Command for IP dumping methods.
 * This command attempts to extract IP addresses from the server using command completion requests.
 */
public class DumpCommand extends Command {
  
    /**
     * Constructor for Dump command.
     * Sets up command name "dump" and description "IP dumping methods".
     */
    public DumpCommand() {
        super("dump", "IP dumping methods", CommandManager.CommandCategory.MISC);
    }

    /**
     * Builds the command structure.
     * The command executes a single action that triggers IP dumping via command completion.
     * 
     * @param root The root literal argument builder to construct the command
     */
    @Override 
    public void build(LiteralArgumentBuilder<CommandSource> root) {
        // Command executes without any arguments
        root.executes(context -> {
            // Generate a random request ID to use for the command completion request
            AtlasClient.MISC_MOD.requestId = new Random().nextInt();
            
            // Send a command completion packet for "/ip " command
            // This may trigger server responses that contain IP information
            Helper.sendPacket(new RequestCommandCompletionsC2SPacket(
                AtlasClient.MISC_MOD.requestId, 
                "/ip "  // Command to request completions for - may reveal player IP information
            ));
            
            // Set the dumping flag to true to indicate IP dumping is in progress
            AtlasClient.MISC_MOD.isDumping = true;
            
            // Notify user that IP dumping attempt has started
            Helper.printChatMessage("Attempting to dump IPs via bungee /ip method!");
            
            // Return success code
            return Command.SINGLE_SUCCESS;
        });
    }
}
