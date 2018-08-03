package arreat.api.net;

import arreat.api.message.Message;

public interface FailStrategy {

    boolean produceMessage();
    Message getMessage();
    void execute();
}
