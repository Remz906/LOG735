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
import arreat.api.message.RegistryQueryMessage;
import arreat.api.net.Packet;
import arreat.api.net.Receiver;
import arreat.api.net.Socket;
import arreat.api.net.Writer;
import arreat.api.registry.entry.Entry;
import arreat.api.registry.query.RegistryQuery;
import arreat.api.registry.query.RegistryQuery.Action;
import arreat.app.message.DefaultNetMessage;
import arreat.app.message.DefaultRegistryQueryMessage;
import arreat.app.pubsub.AbstractMessageQueue;
import arreat.app.registry.DefaultRegistryQuery;
import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Arrays;

/**
 * Implement for a standard receiver, allow retrieval of message and queue them for the net service
 * to process them.
 */
public class DefaultReceiver extends AbstractMessageQueue implements Receiver {

  private Socket socket;

  /**
   * Configure the configurable the class to be ready for use.
   *
   * @param cfg The configuration use to configure the configurable.
   */
  @Override
  public void configure(Configuration cfg) {

  }

  /**
   * Set the net socket the component should be using to communicate.
   * @param socket  The net socket to use.
   */
  @Override
  public void setSocket(Socket socket) {
    this.socket = socket;
  }

  /**
   * Asynchronous execution of the component to allow the packet to be retrieve from the socket.
   * Since the socket can block execution we need a separate thread for this.
   */
  @Override
  public void run() {
    while (true) {
      try {
        Packet packet = this.socket.receive();

        if (packet != null) {
          NetMessage msg = DefaultNetMessage.fromString(packet.getData());

          // Check if the message can be properly reformed otherwise it is dropped.
          if (msg != null) {

            // Update the message to contain the information of who sent it.
            // This is due to the ack process that requires to kno
            msg.setIp(new String(((InetSocketAddress) packet.getAddress()).getAddress().getAddress()));
            msg.setPort(((InetSocketAddress) packet.getAddress()).getPort());

            // Check if we can update information about a registry entry, if so we create a query to
            // update it.
            RegistryQueryMessage query = this.createRegistryUpdate(packet.getAddress(), msg);

            // If it is null that mean the message cannot allow a query to be created.
            if (query != null) {
              this.queue(query);
            }

            // Queue the net message to be published in the service bus by the net service.
            this.queue(msg);
          }
        }
      } catch (Exception ok) {
        // We don't want to stop listening to the socket even if an exception is thrown at some
        // point.
      }
    }
  }

  /**
   * Creates a registry query message to update the registry entry contain in the message if any.
   *
   * @param address The address that will be associated with the registry entry.
   * @param message The message that might contain a registry entry.
   *
   * @return  The registry query message or null if no update can be done.
   */
  private RegistryQueryMessage createRegistryUpdate(SocketAddress address, NetMessage message) {
    Class<?> clz = message.getMessage().getClass();
    RegistryQueryMessage qMsg = null;

    do {
      for (Field f : clz.getDeclaredFields()) {
        if (Entry.class.isAssignableFrom(f.getType()) && f.isAnnotationPresent(Writer.class)) {
          f.setAccessible(true);

          try {
            Entry e = ((Entry) f.get(message.getMessage()));

            // Update the writer values.
            e.setInetAddress(((InetSocketAddress) address).getAddress());
            e.setPort(((InetSocketAddress) address).getPort());

            RegistryQuery query = new DefaultRegistryQuery();

            query.setTarget(e.getClass());
            query.setAction(Action.UPDATE);
            query.setEntry(e);

            qMsg = new DefaultRegistryQueryMessage();
            qMsg.setRecipient(null);
            qMsg.setQuery(query);

          } catch (IllegalAccessException ok) {
            // Won't be thrown we bypass accessibility.
          }
        }
      }
    } while (Object.class.equals(clz = clz.getSuperclass()));

    return qMsg;
  }
}
