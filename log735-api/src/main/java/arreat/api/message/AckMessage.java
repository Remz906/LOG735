package arreat.api.message;

public interface AckMessage extends Message {

    String getHash();
    void setHash(String hash);
}
