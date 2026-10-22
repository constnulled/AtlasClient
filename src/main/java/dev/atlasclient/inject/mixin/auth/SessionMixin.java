package dev.atlasclient.inject.mixin.auth;

import net.minecraft.client.session.Session;
import dev.atlasclient.inject.accessor.SessionAccessor;
import org.spongepowered.asm.mixin.*;

import java.util.UUID;
@SuppressWarnings("unused") @Mixin(Session.class) public class SessionMixin implements SessionAccessor {

  @Final @Shadow @Mutable 
  private String username;

  @Final @Shadow @Mutable 
  private UUID uuid;

  @Override 
  public void atlasClient$setUsername(String username) {
    this.username = username;
  }
  @Override 
  public void atlasClient$setUUID(UUID uuid) {
    this.uuid = uuid;
  }
}
