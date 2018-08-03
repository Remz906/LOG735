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

package arreat.api.pubsub;

import arreat.api.message.Message;
import arreat.api.service.Service;
import java.util.Collection;

/**
 * Defines a service bus use by all the services to publish and subscribe to different message
 * types.
 */
public interface ServiceBus {

  /**
   * Publish a message through the service bus.
   *
   * @param message The message that will be published inside the bus.
   */
  void publish(Message message);

  /**
   * Subscribe to a type of message.
   *
   * @param service The service that subscribe to a specific type of message.
   * @param type    The message type the service is subscribing to.
   */
  void subscribe(Service service, Class<? extends Message> type);

  /**
   * Subscribe to multiple type of messages.
   *
   * @param service The service that subscribe to multiple type of messages.
   * @param types   The messages type the service is subscribing to.
   */
  void subscribe(Service service, Collection<Class<? extends Message>> types);
}
