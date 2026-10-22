package dev.atlasclient.inject.mixin.network.handler;

import net.minecraft.client.network.ClientLoginNetworkHandler;
import net.minecraft.network.packet.s2c.login.LoginSuccessS2CPacket;
import dev.atlasclient.event.bus.EventBus;
import dev.atlasclient.event.impl.network.LoginEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientLoginNetworkHandler.class) public class ClientLoginNetworkHandlerMixin {
  @Inject(method = "onSuccess", at = @At("HEAD"), cancellable = true)
  public void onSucess(LoginSuccessS2CPacket packet, CallbackInfo ci) {
    EventBus.fire(EventBus.LOGIN_EVENT_CHANNEL, new LoginEvent(packet.profile()));
  }
}
