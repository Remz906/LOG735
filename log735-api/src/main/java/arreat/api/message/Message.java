package arreat.api.message;

public interface Message {
    String getContent();
    String[] getReceivers();
    String getSender();
    void setContent(String content);
    void setReceivers(String[] receivers);
    void setSender(String sender);
}