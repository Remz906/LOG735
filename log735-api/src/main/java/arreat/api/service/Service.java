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

package arreat.api.service;

import arreat.api.cfg.Configurable;
import arreat.api.pubsub.Publisher;
import arreat.api.pubsub.Subscriber;

/**
 * Defines a service used by the Arreat App. Services are acting as pub-sub that produce and
 * consumes messages.
 *
 * <p>Services follow a standard lifecycle, they are first configured, register for subscription
 * through the getConsumedMessagesType method that tells the service bus which message it wants to
 * consumed. The the service is started which allows it to publish since it knows the event bus, if
 * need finally the service is terminated at the end of it's useful life.
 */
public interface Service extends Configurable, Publisher, Runnable, Subscriber {

  /**
   * @return Whether the service should be run asynchronously or not.
   */
  boolean asynchronous();

  /**
   * Call any actions that are need to be perform when the service is shutting down.
   */
  void terminate();
}