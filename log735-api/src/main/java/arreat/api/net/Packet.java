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

package arreat.api.net;

import java.net.SocketAddress;

/**
 * Defines a packet that can be send and received by the Socket.
 */
public interface Packet {

  /**
   * Address to which the packet will be send or the address from which it comes from in case of
   * a receive.
   *
   * @return  The socket address.
   */
  SocketAddress getAddress();

  /**
   * The data as string that will be sent or received by the packet through the socket.
   *
   * @return  The data.
   */
  String getData();

  /**
   * Address to which the packet will be send or the address from which it comes from in case of
   * a receive.
   *
   * @param address The socket address.
   */
  void setAddress(SocketAddress address);

  /**
   * The data as string that will be sent or received by packet through the socket.
   *
   * @param data  The data.
   */
  void setData(String data);
}
