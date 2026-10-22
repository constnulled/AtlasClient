package dev.atlasclient.command.impl;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandSource;
import dev.atlasclient.command.Command;
import dev.atlasclient.command.CommandManager;
import dev.atlasclient.inject.accessor.ClientPlayNetworkHandlerAccessor;

public class SayCommand extends Command {
  public SayCommand() {
    super("say", "Sends a chat message to the server", CommandManager.CommandCategory.UTILITY);
  }

  @Override public void build(LiteralArgumentBuilder<CommandSource> root) {
    root.then(argument("message", StringArgumentType.greedyString()).executes(context -> {
      String message = context.getArgument("message", String.class);
      ((ClientPlayNetworkHandlerAccessor) MinecraftClient.getInstance()
        .getNetworkHandler()).atlasClient$sendChatMessage(message);
      return SINGLE_SUCCESS;
    })).executes(this::incompleteCommand);
  }
}
