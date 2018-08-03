package arreat.api.net;

import arreat.api.cfg.Configurable;
import arreat.api.pubsub.MessageQueue;

/**
 * Receiver is in charge receiving net message through the net socket. The message are received in
 * an asynchronous way.
 */
public interface Receiver extends Configurable, MessageQueue, NetComponent, Runnable {

}
