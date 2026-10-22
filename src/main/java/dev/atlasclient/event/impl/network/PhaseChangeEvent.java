package dev.atlasclient.event.impl.network;

import net.minecraft.network.NetworkPhase;

/**
 * This event is fired when the network phase is changed.
 */
public record PhaseChangeEvent(NetworkPhase phase) {
  public PhaseChangeEvent() {
    this(null);
  }
}
