package dev.atlasclient.inject.mixin.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.*;
import dev.atlasclient.themes.ThemeManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to apply themed rendering to all buttons in the game
 */
@Mixin(PressableWidget.class) public class PressableWidgetMixin {
  @Inject(method = "renderWidget", at = @At("HEAD"), cancellable = true)
  public void renderThemedButton(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
    PressableWidget self = (PressableWidget) (Object) this;

    if (!self.visible || self.getWidth() <= 0 || self.getHeight() <= 0) {
      return;
    }

    boolean hovered = self.isHovered();
    boolean pressed = self.isFocused() || self.isSelected();

    ThemeManager.renderButton(context,
      self.getX(),
      self.getY(),
      self.getWidth(),
      self.getHeight(),
      hovered,
      pressed,
      self.getMessage().getString(),
      MinecraftClient.getInstance().textRenderer);

    ci.cancel();
  }
}