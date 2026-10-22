package dev.atlasclient.inject.mixin.etc;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;
import dev.atlasclient.AtlasClient;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class) public abstract class ClientPlayerEntityMixin {
  @Inject(method = "tick", at = @At("TAIL")) public void tick(CallbackInfo ci) {
    Text msg;
    while ((msg = AtlasClient.MISC_MOD.delayedMessages.poll()) != null) {
      this.sendMessage(msg, false);
    }
  }

  @Shadow public abstract void sendMessage(Text message, boolean overlay);

  @Unique private void sendMessage(Text message) {
    this.sendMessage(message, false);
  }
}
