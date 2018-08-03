package arreat.api.net;

import arreat.api.cfg.Configurable;

/**
 * Defines a net socket use to send and receive packets.
 */
public interface Socket extends Configurable {

  /**
   * Sends a packet through the socket.
   *
   * @param packet  Packet to send.
   */
  void send(Packet packet);

  /**
   * Receives a packet through the socket.
   *
   * @return  Packet received.
   */
  Packet receive();
}
