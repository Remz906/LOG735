/*
 * MIT License
 *
 * Copyright (c) 2018 Michael Buron, Olivier Grégoire, Rémi St-André
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package arreat.app.net;

import arreat.api.cfg.Configuration;
import arreat.api.net.Packet;
import arreat.api.net.Socket;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

/**
 * Implementation of net socket for an udp channel that binds a port for both receiving and sending
 * that through the same port. This allows hole punching through NAts.
 */
public class UdpSocket implements Socket {

  private static final String SOCKET_BUFFER_SIZE_PROP = "bufferSize";
  private static final String SOCKET_PORT_PROP = "socketPort";

  private static final int BUFFER_DEFAULT_SIZE = 1024;

  private int bufferSize;
  private DatagramChannel channel;

  /**
   * Configure the configurable the class to be ready for use.
   *
   * @param cfg The configuration use to configure the configurable.
   */
  @Override
  public void configure(Configuration cfg) {
    if (!cfg.exists(SOCKET_PORT_PROP)) {
      throw new RuntimeException("Unable to configure UdpSocket missing port.");
    }
    int port = cfg.getInteger(SOCKET_PORT_PROP);

    if (port <= 1023) {
      throw new RuntimeException("Unable to configure UdpSocket invalid port provided.");
    }

    try {
      this.channel = DatagramChannel.open();

      this.channel.socket().bind(new InetSocketAddress(port));

    } catch (IOException err) {
      throw new RuntimeException("Unable to configure UdpSocket fail to open socket", err);
    }

    this.bufferSize = cfg.exists(SOCKET_BUFFER_SIZE_PROP)
        && cfg.getInteger(SOCKET_BUFFER_SIZE_PROP) > BUFFER_DEFAULT_SIZE
        ? cfg.getInteger(SOCKET_BUFFER_SIZE_PROP) : BUFFER_DEFAULT_SIZE;
  }

  /**
   * Receives a packet through the socket.
   *
   * @return  Packet received.
   */
  @Override
  public Packet receive() {
    Packet packet = new DefaultPacket();
    ByteBuffer buf = ByteBuffer.allocate(this.bufferSize);
    buf.clear();

    try {
      packet.setAddress(channel.receive(buf));
      packet.setData(new String(buf.array()).trim());

    } catch (IOException e) {
      e.printStackTrace();
    }
    return "".equals(packet.getData()) ? null : packet;
  }

  /**
   * Sends a packet through the socket.
   *
   * @param packet  Packet to send.
   */
  @Override
  public void send(Packet packet) {
    ByteBuffer buf = ByteBuffer.allocate(packet.getData().getBytes().length);
    buf.clear();
    buf.put(packet.getData().getBytes());
    buf.flip();

    try {
      this.channel.send(buf, packet.getAddress());

    } catch (IOException ok) {
      // We ignore this, might happen but should not.
    }
  }
}
