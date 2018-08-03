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
import arreat.api.message.AckMessage;
import arreat.api.message.NetMessage;
import arreat.api.net.Acknowledger;
import arreat.api.net.FailStrategy;
import arreat.app.message.DefaultAckMessage;
import arreat.app.message.DefaultNetMessage;
import arreat.app.pubsub.AbstractMessageQueue;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class DefaultAcknowledger extends AbstractMessageQueue implements Acknowledger {

  private static final String MAX_DELAY_RETRY_PROP = "maxDelayRetry";
  private static final String MAX_DELAY_RETRY_TIME_UNIT_PROP = "maxDelayRetryTimeUnit";

  private static final TimeUnit DEFAULT_DELAY_RETRY_TIME_UNIT = TimeUnit.MILLISECONDS;

  private final Map<String, AcknowledgerEntry> pending;
  private final Map<String, AcknowledgerEntry> history;

  private int maxRetryDelay;

  private TimeUnit maxRetryDelayTimeUnit;

  public DefaultAcknowledger() {
    this.pending = new LinkedHashMap<>();
    this.history = new LinkedHashMap<>();
  }

  @Override
  public void add(NetMessage message) {

    // We ignore the ACK message since we don't wont to ACK and ACK.
    if (!(message.getMessage() instanceof AckMessage)) {

      // Check if we already have the message decrease the max retries, this means the message is a
      // retry, managing message that have reach the max is done in the separate thread.
      if (this.pending.containsKey(message.getHash())) {
        this.pending.get(message.getHash()).getMessage()
            .setMaxRetries(this.pending.get(message.getHash()).getMessage().getMaxRetries() - 1);

      } else {
        this.pending.put(message.getHash(), new AcknowledgerEntry(message));
      }
    }
  }

  @Override
  public NetMessage acknowledge(NetMessage message) {
    NetMessage ackMsg = null;

    if (message.getMessage() instanceof AckMessage
        && this.pending.containsKey(((AckMessage) message.getMessage()).getHash())) {

      this.pending.remove(((AckMessage) message.getMessage()).getHash());

    } else {
      if (this.history.containsKey(message.getHash())) {
        ackMsg = this.history.get(message.getHash()).getMessage();

      } else {
        ackMsg = new DefaultNetMessage(message.getIp(), message.getPort(),
            new DefaultAckMessage(message.getHash()));
      }
    }
    return ackMsg;
  }

  @Override
  public void configure(Configuration cfg) {
    if (!cfg.exists(MAX_DELAY_RETRY_PROP) || cfg.getInteger(MAX_DELAY_RETRY_PROP) < 1) {
      throw new RuntimeException("Unable to configure default acknowledger, missing max delay retry.");
    }
    this.maxRetryDelay = cfg.getInteger(MAX_DELAY_RETRY_PROP);

    if (cfg.exists(MAX_DELAY_RETRY_TIME_UNIT_PROP)) {
      try {
        this.maxRetryDelayTimeUnit =
            TimeUnit.valueOf(cfg.getString(MAX_DELAY_RETRY_TIME_UNIT_PROP).toUpperCase());

      } catch (IllegalArgumentException err) {
        throw new RuntimeException("Unable to set time unit property of heartbeat service, unknown time unit");
      }
    } else {
      this.maxRetryDelayTimeUnit = DEFAULT_DELAY_RETRY_TIME_UNIT;
    }
  }

  @Override
  public void run() {
    while (true) {
      try {
        this.maxRetryDelayTimeUnit.sleep(this.maxRetryDelay);

        Set<String> hashToRemove = new HashSet<>();

        // Cycle through the net message with a pending ACK.
        for (Entry<String, AcknowledgerEntry> entry : this.pending.entrySet()) {
          NetMessage netMsg = entry.getValue().getMessage();

          // If we reached the max retries we remove the message from the pending queue.
          if (netMsg.isMaxRetriesReach()) {
            hashToRemove.add(entry.getKey());

            // Check if we need to execute a strategy on failure of delivery.
            if (netMsg.getFailStrategy() != null) {
              FailStrategy strategy = netMsg.getFailStrategy();

              strategy.execute();

              // Check if the execution produce a message, if so we queue it so the net service can
              // send it through the service bus.
              if (strategy.produceMessage()) {
                this.queue(strategy.getMessage());
              }
            }
          }
        }

        for (String hash : hashToRemove) {
          this.pending.remove(hash);
        }

      } catch (InterruptedException e) {
        // Should not happen, if it does we sleep again.
      }
    }
  }

  private static class AcknowledgerEntry {

    private final NetMessage message;
    private final long timestamp;

    private AcknowledgerEntry(NetMessage message) {
      this.message = message;
      this.timestamp = new Date().getTime();
    }

    public NetMessage getMessage() {
      return message;
    }

    public long getTimestamp() {
      return timestamp;
    }
  }
}
