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

package arreat.app.pubsub;

import arreat.api.message.Message;
import arreat.api.pubsub.MessageQueue;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Abstract implementation of message queue to avoid code duplication.
 */
public abstract class AbstractMessageQueue implements MessageQueue {

  private final Queue<Message> queue;

  /**
   * Default constructor for the message queue.
   */
  public AbstractMessageQueue() {
    this.queue = new LinkedList<>();
  }

  /**
   * Allow message to be added to the queue.
   *
   * @param message Message to add.
   */
  protected void queue(Message message) {
    this.queue.add(message);
  }

  /**
   * Returns the next message in the queue or null if there are no message.
   *
   * @return  The next message.
   */
  @Override
  public Message getMessage() {
    return this.queue.poll();
  }

  /**
   * Tells if the queue contain any messages.
   *
   * @return  Whether there is one or more message in the queue.
   */
  @Override
  public boolean hasMessage() {
    return !this.queue.isEmpty();
  }
}
