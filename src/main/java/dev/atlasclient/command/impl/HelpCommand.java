package dev.atlasclient.command.impl;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import dev.atlasclient.*;
import dev.atlasclient.command.Command;
import dev.atlasclient.command.CommandManager;

/**
 * Represents a command that displays help information for other commands.
 *
 * @author SpigotRCE
 * @since 1.4
 */
public class HelpCommand extends Command {

  /**
   * Constructs a new instance of {@link HelpCommand}.
   */
  public HelpCommand() {
    super("help", "Shows help page", CommandManager.CommandCategory.UTILITY);
  }

  /**
   * Builds the command structure using Brigadier's {@link LiteralArgumentBuilder}.
   */
  @Override public void build(LiteralArgumentBuilder<CommandSource> root) {
    root.executes(context -> {
      Helper.printChatMessage("&8&m-----------------------------------------------------", false);
      Helper.printChatMessage("&4&l[Command List]");
      for (Command command : AtlasClient.COMMAND_MANAGER.getCommands()) {
        Helper.printChatMessage("&b - &a" + command.getName() + " &8| &f" + command.getDescription());
      }
      Helper.printChatMessage("&4 - &fTotal Registered: &4" + AtlasClient.COMMAND_MANAGER.getCommands().size());
      Helper.printChatMessage("&8&m-----------------------------------------------------", false);
      return SINGLE_SUCCESS;
    }).then(argument("command", StringArgumentType.word()).executes(context -> {
      String name = context.getArgument("command", String.class);
      Command command = AtlasClient.COMMAND_MANAGER.getCommand(name);

      if (command == null) {
        Helper.printChatMessage("&4&l[Error] &cCommand not found: &4" + name);
        return SINGLE_SUCCESS;
      }

      Helper.printChatMessage("&8&m-----------------------------------------------------", false);
      Helper.printChatMessage("&4&l[Command Info]");
      Helper.printChatMessage("&5 - &aName: &b" + command.getName());
      Helper.printChatMessage("&7 - &aDescription: &b" + command.getDescription());
      Helper.printChatMessage("&8&m-----------------------------------------------------", false);
      return SINGLE_SUCCESS;
    }).suggests((context, builder) -> {
      String partialName;
      try {
        partialName = context.getArgument("command", String.class);
      } catch (IllegalArgumentException e) {
        partialName = "";
      }

      String finalPartialName = partialName;
      AtlasClient.COMMAND_MANAGER.getCommands()
        .stream()
        .map(Command::getName)
        .filter(cmd -> cmd.toLowerCase().startsWith(finalPartialName.toLowerCase()))
        .forEach(builder::suggest);

      return builder.buildFuture();
    }));
  }
}
