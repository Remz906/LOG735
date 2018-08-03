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

package arreat.api.message;

import arreat.api.net.FailStrategy;

/**
 * Defines a message to subscribe to the heartbeat service. This is usually done through the cfg
 * but in case of dynamic needs this message can be used.
 */
public interface HeartbeatSubscriptionMessage extends Message {

  /**
   * The ip targeted by the heartbeat.
   *
   * @return  String representing the ip address of the target.
   */
  String getIp();

  /**
   * The port targeted by the heartbeat.
   *
   * @return  representing the port of the target.
   */
  int getPort();

  /**
   * The strategy that will be executed if the heartbeat does not have a response.
   *
   * @return  the strategy to execute on heartbeat fail.
   */
  FailStrategy getStrategy();

  /**
   * The ip targeted by the heartbeat.
   *
   * @param ip  String representing the ip address of the target.
   * @throws IllegalArgumentException Thrown if the format is not a valid IPv4 or IPv6 Address
   */
  void setIp(String ip);

  /**
   * The port targeted by the heartbeat.
   *
   * @param port representing the port of the target.
   */
  void setPort(int port);

  /**
   * The strategy that will be executed if the heartbeat does not have a response.
   *
   * @param strategy  the strategy to execute on heartbeat fail.
   */
  void setStrategy(FailStrategy strategy);
}
