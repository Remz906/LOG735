package arreat.api.net;

import arreat.api.cfg.Configurable;
import arreat.api.message.NetMessage;
import arreat.api.pubsub.MessageQueue;

public interface Acknowledger extends Configurable, MessageQueue, Runnable {

  void add(NetMessage message);

  NetMessage acknowledge(NetMessage message);
}
