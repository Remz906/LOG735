package arreat.core;

import java.util.Objects;

public class Message {

  private String DiscussionName;
  private int timestamp;
  private String pseudo;
  private String message;

  public Message(String discussionName, int timestamp, String pseudo, String message) {
    DiscussionName = discussionName;
    this.timestamp = timestamp;
    this.pseudo = pseudo;
    this.message = message;
  }

  public String getDiscussionName() {
    return DiscussionName;
  }

  public int getTimestamp() {
    return timestamp;
  }

  public String getPseudo() {
    return pseudo;
  }

  public String getMessage() {
    return message;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Message message1 = (Message) o;
    return timestamp == message1.timestamp &&
        Objects.equals(DiscussionName, message1.DiscussionName) &&
        Objects.equals(pseudo, message1.pseudo) &&
        Objects.equals(message, message1.message);
  }

}
