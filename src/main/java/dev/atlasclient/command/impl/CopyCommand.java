package dev.atlasclient.command.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import dev.atlasclient.Helper;
import dev.atlasclient.command.Command;
import dev.atlasclient.command.CommandManager;

import java.awt.*;
import java.awt.datatransfer.*;

/**
 * This class represents a command that copies a specific broadcast message to the clipboard. The command is part of the
 * AtlasClient mod for Minecraft.
 *
 * @author SpigotRCE
 * @version 1.0
 */
public class CopyCommand extends Command {

  /**
   * Constructs a new CopyCommand instance.
   * Sets up the command name "copy" and description "Copies the broadcast of SpigotRCE".
   */
  public CopyCommand() {
    super("copy", "Copies the broadcast of SpigotRCE", CommandManager.CommandCategory.MISC);
  }

  /**
   * Builds the command structure using Brigadier's LiteralArgumentBuilder. 
   * The command has two execution paths:
   * 1. Default execution - copies a formatted text message
   * 2. With "tellraw" subcommand - copies a complex tellraw JSON message
   * 
   * @param root The root literal argument builder to construct the command
   */
  @Override 
  public void build(LiteralArgumentBuilder<CommandSource> root) {
    // Build command with "tellraw" subcommand
    root.then(literal("tellraw").executes((context) -> {
      // Creates a StringSelection object containing a tellraw message with clickable URL
      StringSelection stringSelection = new StringSelection(
        "tellraw @a [{\"text\":\"Server hacked by\\n\", \"color\":\"green\"},{\"text\":\"https://youtube.com/@SpigotRCE\", \"color\":\"aqua\", \"bold\":true, \"clickEvent\":{\"action\":\"open_url\",\"value\":\"https://youtube.com/@SpigotRCE\"}}]");
      
      // Gets the system clipboard and sets the contents to the tellraw message
      Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
      clipboard.setContents(stringSelection, null);
      
      // Notifies the user that the tellraw message has been copied
      Helper.printChatMessage("SpigotRCE's tellraw has been copied to your clipboard.");
      return SINGLE_SUCCESS;
    }))
    // Default execution when no subcommand is provided
    .executes((context) -> {
      // Creates a StringSelection object containing a formatted text message
      StringSelection stringSelection = new StringSelection("&cServer hacked by&5&l https://youtube.com/@SpigotRCE");
      
      // Gets the system clipboard and sets the contents to the formatted message
      Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
      clipboard.setContents(stringSelection, null);
      
      // Notifies the user that the broadcast message has been copied
      Helper.printChatMessage("SpigotRCE's broadcast has been copied to your clipboard.");
      return SINGLE_SUCCESS;
    });
  }
}
