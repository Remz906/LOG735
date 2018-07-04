package arreat.api.message;

public class AcknowledgementMessage implements Message {

    private String content;
    private String[] receivers;
    private String sender;

    public AcknowledgementMessage() {
        this.content = null;
        this.receivers = null;
        this.sender = null;
    }

    @Override
    public String getContent() {
        return this.content;
    }

    @Override
    public String[] getReceivers() {
        return this.receivers;
    }

    @Override
    public String getSender() {
        return this.sender;
    }

    @Override
    public void setContent(String content) {
        if (content == null || content.matches("[^\\s]+")) {
            throw new IllegalArgumentException("Acknowledgement must have a hash.");
        }
        this.content = content;
    }

    @Override
    public void setReceivers(String[] receivers) {
        if (receivers == null || receivers.length != 1) {
            throw new IllegalArgumentException("Acknowledgement must have a single receiver.");
        }
        this.receivers = receivers;
    }

    @Override
    public void setSender(String sender) {
        if (sender == null) {
            throw new IllegalArgumentException("Acknowledgement must have a sender.");
        }
        this.sender = sender;
    }
}
