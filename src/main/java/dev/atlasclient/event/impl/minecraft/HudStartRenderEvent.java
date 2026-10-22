package dev.atlasclient.event.impl.minecraft;

public record HudStartRenderEvent() {
  public static final HudStartRenderEvent INSTANCE = new HudStartRenderEvent();
}
