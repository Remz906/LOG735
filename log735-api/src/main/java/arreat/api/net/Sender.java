package arreat.api.net;

import arreat.api.cfg.Configurable;
import arreat.api.message.NetMessage;

/**
 * Defines a sender that allows message to be sent through a socket.
 */
public interface Sender extends Configurable, NetComponent {

    /**
     * Send a net message through the socket.
     *
     * @param message The message to send.
     */
    void send(NetMessage message);
}
