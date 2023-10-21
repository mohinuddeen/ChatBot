package com.mohinuddeen.chatbotjv;

public class Message {
  public static String SENT_BY_ME = "me";
  public static String SENT_BY_BOT="bot";

  String message;
  String sentBy;
  String TypedBy;

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public String getSentBy() {
    return sentBy;
  }

  public void setSentBy(String sentBy) {
    this.sentBy = sentBy;
  }

  public String getTypedBy() {
    return TypedBy;
  }

  public void setTypedBy(String typedBy) {
    TypedBy = typedBy;
  }

  public Message(String message, String sentBy, String TypedBy) {
    this.message = message;
    this.sentBy = sentBy;
    this.TypedBy = TypedBy;

  }
}
