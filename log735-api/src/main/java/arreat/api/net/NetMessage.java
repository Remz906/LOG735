package arreat.api.net;

public class NetMessage {

    private String content;
    private String hash;
    private String recipient;
    private String sender;
    private String wrapped;

    NetMessage() {
        this.content = null;
        this.hash = null;
        this.recipient = null;
        this.sender = null;
        this.wrapped = null;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getWrapped() {
        return wrapped;
    }

    public void setWrapped(String wrapped) {
        this.wrapped = wrapped;
    }
}
