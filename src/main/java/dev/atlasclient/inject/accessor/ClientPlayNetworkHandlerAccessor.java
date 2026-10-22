package dev.atlasclient.inject.accessor;

import dev.atlasclient.event.impl.chat.*;

public interface ClientPlayNetworkHandlerAccessor {
  /**
   * Sends a chat message without firing the {@link ChatPostEvent} or {@link ChatPreEvent}.
   *
   * @param message The message to be sent.
   */
  void atlasClient$sendChatMessage(String message);
}
