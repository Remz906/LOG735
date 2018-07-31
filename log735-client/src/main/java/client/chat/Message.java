package client.chat;

import com.google.gson.Gson;

public class Message {

    private String sender;
    private String recipient;
    private String body;

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    @Override
    public String toString() {
        Gson gson = new Gson();
        return String.format("CMSG:%s",gson.toJson(this));
    }

    public static Message fromJson(String s) {
        Gson gson = new Gson();
        return gson.fromJson(s, Message.class);
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }
}
