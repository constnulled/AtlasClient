package dev.atlasclient.event.impl.network;

import com.mojang.authlib.GameProfile;

public record LoginEvent(GameProfile profile) {
  public LoginEvent() {
    this(null);
  }
}
