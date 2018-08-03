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

package arreat.app.service;

import arreat.api.cfg.Configuration;
import arreat.api.message.Message;
import arreat.api.message.NetMessage;
import arreat.api.net.Acknowledger;
import arreat.api.net.Socket;
import arreat.api.net.Receiver;
import arreat.api.net.Sender;
import com.google.common.eventbus.Subscribe;
import com.google.common.util.concurrent.MoreExecutors;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Default service used by the Arreat App to interact with the network layer. Used to collect
 * messages.
 *
 * <p>Contains a net socket, which binds a socket to allow sending and receiving of data. Data is
 * wrapped as NetMessage to give an higher abstraction level.
 *
 * <p>All components implementation can be overridden by the NetService configuration.
 */
public class NetService extends AbstractService {

  private static final String ACKNOWLEDGER_DEFAULT_IMPL = "arreat.app.net.DefaultAcknowledger";
  private static final String ACKNOWLEDGER_IMPL_PROPERTY = "acknowledgerImpl";

  private static final String RECEIVER_DEFAULT_IMPL = "arreat.app.net.DefaultReceiver";
  private static final String RECEIVER_IMPL_PROPERTY = "receiverImpl";

  private static final String SENDER_DEFAULT_IMPL = "arreat.app.net.DefaultSender";
  private static final String SENDER_IMPL_PROPERTY = "senderImpl";

  private static final String SOCKET_DEFAULT_IMPL = "arreat.app.net.UDPSocket";
  private static final String SOCKET_IMPL_PROPERTY = "socketImpl";

  private final ExecutorService threads;

  private Acknowledger acknowledger;
  private Receiver receiver;
  private Sender sender;
  private boolean terminated;

  /**
   * Default Service constructor.
   */
  public NetService() {
    this.terminated = false;

    this.threads = MoreExecutors.getExitingExecutorService((ThreadPoolExecutor)
        Executors.newFixedThreadPool(1), 1000, TimeUnit.MILLISECONDS);
  }

  @Override
  public boolean asynchronous() {
    return true;
  }

  /**
   * Configure the configurable the class to be ready for use.
   *
   * @param cfg The configuration use to configure the configurable.
   */
  @Override
  public void configure(Configuration cfg) {
    // Create and configure the Acknowledger.
    this.acknowledger = this.createAcknowledger(cfg);

    // Create and configure the Receiver.
    this.receiver = this.createReceiver(cfg);

    // Create and configure the Sender.
    this.sender = this.createSender(cfg);

    // Create and configure the Net Socket.
    Socket socket = this.createSocket(cfg);

    // Init all component of the NetService.
    this.sender.setSocket(socket);
    this.receiver.setSocket(socket);

    // Start the threads for concurrent components.
    this.threads.submit(this.receiver);
    this.threads.submit(this.acknowledger);
  }

  /**
   * Returns a set of Message implementation that the service needs to consume.
   * @return  Set of types that need to be consumed by the service.
   */
  @Override
  public Set<Class<? extends Message>> getConsumedMessagesType() {
    return Collections.singleton(NetMessage.class);
  }

  /**
   * Handles net message, this is used by the default service bus. The net send the message through
   * the net work and create an acknowledgement entry to insure the message is properly sent.
   *
   * @param message The net message that was published.
   */
  @Subscribe
  public void handleNetMessage(NetMessage message) {
    if (message == null) {
      throw new IllegalArgumentException("Net message cannot be null.");
    }

    // Add the message to wait for an ACK, if the acknowledger
    // already knows it means it's a retry.
    this.acknowledger.add(message);

    // Send the message through the Socket.
    this.sender.send(message);
  }

  /**
   * Asynchronous side of the service verify the different queues of its component to see if message
   * needs to be published inside the event bus.
   */
  @Override
  public void run() {
    while (!terminated) {

      // Check if there are messages inside the receiver queue.
      if (this.receiver.hasMessage()) {
        Message msg = this.receiver.getMessage();

        // Check if the message remove from the queue is a NetMessage, might be an update query!
        if (msg instanceof NetMessage) {

          // Try to acknowledge the message.
          NetMessage ack = this.acknowledger.acknowledge((NetMessage) msg);

          // If an ack is return we published it through the event bus.
          if (ack != null) {
            this.bus.publish(ack);
          }

          // If the net message contains a message we publish it.
          // If the net message was an ack message at this stage it is empty to avoid loophole.
          if (((NetMessage) msg).getMessage() != null) {
            this.bus.publish(((NetMessage) msg).getMessage());

          } else {
            this.bus.publish(msg);
          }
        }
      }

      // Check if there are messages inside the acknowledger queue.
      if (this.acknowledger.hasMessage()) {
        this.bus.publish(this.acknowledger.getMessage());
      }
    }
  }

  /**
   * Call any actions that are need to be perform when the service is shutting down.
   */
  @Override
  public void terminate() {
    this.threads.shutdown();
    this.terminated = true;
  }

  /**
   * Creates a acknowledger instance base on the acknowledgerImpl property of the cfg file. If no
   * implementation is specified default to the default implementation. This allow the
   * implementation to be changed without having to rewrite the NetService.
   *
   * @param cfg The Configuration to create and configure the acknowledger.
   * @return Acknowledger ready to init.
   */
  private Acknowledger createAcknowledger(Configuration cfg) {
    String className = cfg.exists(ACKNOWLEDGER_IMPL_PROPERTY)
        ? cfg.getString(ACKNOWLEDGER_IMPL_PROPERTY) : ACKNOWLEDGER_DEFAULT_IMPL;

    Acknowledger acknowledger;

    try {
      acknowledger = (Acknowledger) this.createAndConfigure(className, cfg);

    } catch (ClassCastException err) {
      throw new RuntimeException("Fail to create acknowledger, expected Acknowledger Implementation.");
    }

    return acknowledger;
  }

  /**
   * Creates a receiver instance base on the receiverImpl property of the cfg file. If no
   * implementation is specified default to the default implementation. This allow the
   * implementation to be changed without having to rewrite the NetService.
   *
   * @param cfg The Configuration to create and configure the receiver.
   * @return Receiver ready to init.
   */
  private Receiver createReceiver(Configuration cfg) {
    String className = cfg.exists(RECEIVER_IMPL_PROPERTY)
        ? cfg.getString(RECEIVER_IMPL_PROPERTY) : RECEIVER_DEFAULT_IMPL;

    Receiver receiver;

    try {
      receiver = (Receiver) this.createAndConfigure(className, cfg);

    } catch (ClassCastException err) {
      throw new RuntimeException("Fail to create receiver, expected Receiver Implementation.");
    }

    return receiver;
  }

  /**
   * Creates a sender instance base on the receiverImpl property of the cfg file. If no
   * implementation is specified default to the default implementation. This allow the
   * implementation to be changed without having to rewrite the NetService.
   *
   * @param cfg The Configuration to create and configure the sender.
   * @return Sender ready to init.
   */
  private Sender createSender(Configuration cfg) {
    String className = cfg.exists(SENDER_IMPL_PROPERTY)
        ? cfg.getString(SENDER_IMPL_PROPERTY) : SENDER_DEFAULT_IMPL;

    Sender sender;

    try {
      sender = (Sender) this.createAndConfigure(className, cfg);

    } catch (ClassCastException err) {
      throw new RuntimeException("Fail to create receiver, expected Receiver Implementation.");
    }

    return sender;
  }

  /**
   * Creates a net socket instance base on the socketImpl property of the cfg file. If no
   * implementation is specified default to the UDPSocket implementation. This allow the
   * implementation to be changed without having to rewrite the NetService.
   *
   * @param cfg The Configuration to create and configure the socket.
   * @return Socket ready to init.
   */
  private Socket createSocket(Configuration cfg) {
    String className = cfg.getProperty(SOCKET_IMPL_PROPERTY) == null
        ? SOCKET_DEFAULT_IMPL : cfg.getString(SOCKET_IMPL_PROPERTY);

    Socket socket;

    try {
      socket = (Socket) this.createAndConfigure(className, cfg);

    } catch (ClassCastException err) {
      throw new RuntimeException("Fail to create socket, expected Socket Implementation.");
    }

    return socket;
  }
}
