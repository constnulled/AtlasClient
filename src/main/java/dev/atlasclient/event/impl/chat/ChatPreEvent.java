package dev.atlasclient.event.impl.chat;

/**
 * Event before the chat message has been sent.
 */
@SuppressWarnings("unused") public class ChatPreEvent {
  private String message;

  public ChatPreEvent(String message) {
    this.message = message;
  }

  public ChatPreEvent() {
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }
}
