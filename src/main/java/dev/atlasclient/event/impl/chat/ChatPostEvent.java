package dev.atlasclient.event.impl.chat;

/**
 * Event after the chat message has been sent.
 */
public record ChatPostEvent(String message) {
  public ChatPostEvent() {
    this(null);
  }
}
