package arreat.api.message;

import arreat.api.net.FailStrategy;
import java.net.InetAddress;

public interface NetMessage extends Message {

    void setIp(String ip);
    InetAddress getIp();
    void setPort(int port);
    int getPort();
    void setMessage(Message message);
    Message getMessage();
    String getHash();
    int getMaxRetries();
    void setMaxRetries(int maxRetries);
    void setFailStrategy(FailStrategy strategy);
    FailStrategy getFailStrategy();
    boolean isMaxRetriesReach();
    Message executeFailStrategy();

    @Override
    default String serialize() {
        return String.format("[%s](%s)[%s]",
                this.getMessage().getClass().getName(),
                this.getMessage().serialize(),
                this.getHash());
    }
}
