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
import arreat.api.message.NetMessage;
import arreat.api.net.Packet;
import arreat.api.net.Socket;
import arreat.api.net.Sender;
import java.net.InetSocketAddress;

/**
 * Default implementation of a sender.
 */
public class DefaultSender implements Sender {

  private Socket socket;

  /**
   * Configure the configurable the class to be ready for use.
   *
   * @param cfg The configuration use to configure the configurable.
   */
  @Override
  public void configure(Configuration cfg) {
    // Noting to configure for this component.
  }

  /**
   * Send a net message through the socket.
   *
   * @param message The message to send.
   */
  @Override
  public void send(NetMessage message) {
    if (this.socket != null) {
      Packet p = new DefaultPacket();

      p.setData(message.serialize());
      p.setAddress(new InetSocketAddress(message.getIp(), message.getPort()));

      this.socket.send(p);
    }
  }

  /**
   * Set the net socket the component should be using to communicate.
   * @param socket  The net socket to use.
   */
  @Override
  public void setSocket(Socket socket) {
    this.socket = socket;
  }
}
