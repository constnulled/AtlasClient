package dev.atlasclient.inject.mixin.chat;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.brigadier.*;
import com.mojang.brigadier.suggestion.Suggestions;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatInputSuggestor;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.command.CommandSource;
import dev.atlasclient.AtlasClient;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.CompletableFuture;


@Mixin(ChatInputSuggestor.class) public abstract class ChatInputSuggestorMixin {
  @Shadow @Final TextFieldWidget textField;
  @Shadow boolean completingSuggestions;
  @Shadow private ParseResults<CommandSource> parse;
  @Shadow private CompletableFuture<Suggestions> pendingSuggestions;
  @Shadow private ChatInputSuggestor.SuggestionWindow window;

  /**
   * To suggest tab completion for atlas registered commands.
   *
   * @param ci
   * @param reader
   */
  @Inject(method = "refresh",
    at = @At(value = "INVOKE", target = "Lcom/mojang/brigadier/StringReader;canRead()Z", remap = false),
    cancellable = true) public void onRefresh(CallbackInfo ci, @Local StringReader reader) {
    String prefix = AtlasClient.COMMAND_MANAGER.prefix;
    int length = prefix.length();

    if (reader.canRead(length) && reader.getString().startsWith(prefix, reader.getCursor())) {
      reader.setCursor(reader.getCursor() + length);

      if (this.parse == null) {
        this.parse = AtlasClient.COMMAND_MANAGER.DISPATCHER.parse(reader,
          MinecraftClient.getInstance().getNetworkHandler().getCommandSource());
      }

      int cursor = textField.getCursor();
      if (cursor >= length && (this.window == null || !this.completingSuggestions)) {
        this.pendingSuggestions =
          AtlasClient.COMMAND_MANAGER.DISPATCHER.getCompletionSuggestions(this.parse, cursor);
        this.pendingSuggestions.thenRun(() -> {
          if (this.pendingSuggestions.isDone()) {
            this.showCommandSuggestions();
          }
        });
      }

      ci.cancel();
    }
  }

  @Shadow protected abstract void showCommandSuggestions();
}
