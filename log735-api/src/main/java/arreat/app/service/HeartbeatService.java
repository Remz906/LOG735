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
import arreat.api.message.HeartbeatSubscriptionMessage;
import arreat.api.message.Message;
import arreat.api.message.NetMessage;
import arreat.api.net.FailStrategy;
import arreat.app.message.DefaultNetMessage;
import com.google.common.eventbus.Subscribe;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Defines the heartbeat service. This service is used to do heartbeat on a list of predefine host.
 */
public class HeartbeatService extends AbstractService {

  private static final String FAIL_STRATEGY_PROP = "strategy";
  private static final String HEARTBEAT_TARGETS_PROP = "targets";
  private static final String TARGET_IP_PROP = "ip";
  private static final String TARGET_PORT_PROP = "port";
  private static final String HEARTBEAT_DELAY_PROP = "delay";
  private static final String HEARTBEAT_TIME_UNIT_PROP = "timeUnit";
  private static final String HEARTBEAT_MAX_RETRIES_PROP = "maxRetries";

  private static final TimeUnit DEFAULT_TIME_UNIT = TimeUnit.MILLISECONDS;

  private static final int DEFAULT_MAX_RETRIES = 2;

  private final Set<HeartbeatEntry> entries;

  private int delay;
  private int maxRetries;
  private TimeUnit timeUnit;
  private boolean terminated;

  /**
   * Default constructor of the service.
   */
  public HeartbeatService() {
    this.entries = new HashSet<>();
    this.terminated = false;
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
  @SuppressWarnings("unchecked")
  public void configure(Configuration cfg) {
    if (!cfg.exists(HEARTBEAT_DELAY_PROP)) {
      throw new RuntimeException("Unable to configure heartbeat service, missing delay property.");
    }

    if (cfg.exists(HEARTBEAT_TARGETS_PROP)) {
      try {
        List<Map<String, Object>> props = (List<Map<String, Object>>) cfg
            .getProperty(HEARTBEAT_TARGETS_PROP);

        for (Map<String, Object> prop : props) {
          this.entries.add(new HeartbeatEntry(
              String.valueOf(prop.get(TARGET_IP_PROP)),
              Integer.parseInt(String.valueOf(prop.get(TARGET_PORT_PROP))),
              this.createStrategy(prop.get(FAIL_STRATEGY_PROP))));
        }
      } catch (ClassCastException | NumberFormatException err) {
        throw new RuntimeException("Unable to load configuration of heartbeat targets, unknown object found expected list.");
      }
    }

    if (cfg.exists(HEARTBEAT_TIME_UNIT_PROP)) {
      try {
        this.timeUnit = TimeUnit.valueOf(cfg.getString(HEARTBEAT_TIME_UNIT_PROP).toUpperCase());

      } catch (IllegalArgumentException err) {
        throw new RuntimeException("Unable to set time unit property of heartbeat service, unknown time unit");
      }
    } else {
      this.timeUnit = DEFAULT_TIME_UNIT;
    }

    this.delay = cfg.getInteger(HEARTBEAT_DELAY_PROP);

    if (this.delay <= 0) {
      throw new RuntimeException("Invalid heartbeat delay, must me greater then 0.");
    }

    this.maxRetries = cfg.exists(HEARTBEAT_MAX_RETRIES_PROP)
        ? cfg.getInteger(HEARTBEAT_MAX_RETRIES_PROP) : DEFAULT_MAX_RETRIES;
  }

  /**
   * Returns a set of Message implementation that the service needs to consume.
   * @return  Set of types that need to be consumed by the service.
   */
  @Override
  public Set<Class<? extends Message>> getConsumedMessagesType() {
    return Collections.singleton(HeartbeatSubscriptionMessage.class);
  }

  /**
   * Handle heartbeat subscription message for the default service bus.
   *
   * @param message The subscription message.
   */
  @Subscribe
  public void handleHeartbeatSubscriptionMessage(HeartbeatSubscriptionMessage message) {
    this.entries.add(new HeartbeatEntry(message.getIp(), message.getPort(), message.getStrategy()));
  }

  /**
   * Asynchronous run of the heartbeat service, will sleep for defined delay. If then will trigger
   * and send heartbeat message to all registered target.
   */
  @Override
  public void run() {
    while (this.terminated) {
      try {
        this.timeUnit.sleep(delay);

        for (HeartbeatEntry entry : this.entries) {
          this.bus.publish(this.createNetMessage(entry));
        }

      } catch (InterruptedException ok) {
        // This should not be thrown, if some how it does we just sleep one more time.
      }
    }
  }

  /**
   * Call any actions that are need to be perform when the service is shutting down.
   */
  @Override
  public void terminate() {
    this.terminated = true;
  }

  /**
   * Creates a net message for an heartbeat, basically its an empty net message that requires
   * an ack.
   *
   * @param entry
   * @return
   */
  private NetMessage createNetMessage(HeartbeatEntry entry) {
    return new DefaultNetMessage(
        entry.getIp(),
        entry.getPort(),
        this.maxRetries,
        new HeartbeatMessage(),
        entry.getStrategy());
  }

  /**
   * Create an instance of the fail strategy base on its class name.
   * @param obj A string representing the name of the fail strategy, if null returns null.
   * @return  Instance of fail strategy.
   */
  private FailStrategy createStrategy(Object obj) {
    FailStrategy strategy = null;

    if (obj instanceof String) {
      try {
        Class<?> clz = Class.forName((String) obj);

        Constructor ctor = clz.getDeclaredConstructor();
        ctor.setAccessible(true);

        strategy = (FailStrategy) ctor.newInstance();

      } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException
          | InstantiationException | InvocationTargetException  ok) {
        // Simply return null.
      }
    }
    return strategy;
  }

  /**
   * Represent an entry for the heartbeat service, those will be interrogated at a set delay to
   * make sure there alive.
   */
  private static class HeartbeatEntry {

    private final String ip;
    private final int port;
    private final FailStrategy strategy;

    private HeartbeatEntry(String ip, int port, FailStrategy strategy) {
      this.ip = ip;
      this.port = port;
      this.strategy = strategy;
    }

    /**
     * @return IP of the target
     */
    public String getIp() {
      return ip;
    }

    /**
     * @return Port of the target
     */
    public int getPort() {
      return port;
    }

    /**
     * @return  Fail Strategy.
     */
    public FailStrategy getStrategy() {
      return strategy;
    }
  }

  /**
   * Heartbeat base message.
   */
  public static class HeartbeatMessage implements Message {

    private long timestamp;

    public HeartbeatMessage() {
      this.timestamp = new Date().getTime();
    }

    public long getTimestamp() {
      return timestamp;
    }

    public void setTimestamp(long timestamp) {
      this.timestamp = timestamp;
    }
  }
}
