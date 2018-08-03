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

package arreat.app.message;

import arreat.api.message.AckMessage;
import arreat.api.message.Message;
import arreat.api.message.NetMessage;
import arreat.api.net.FailStrategy;
import com.google.gson.Gson;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DefaultNetMessage implements NetMessage {

  private static final MessageDigest MD5;
  private static final Pattern NET_MESSAGE_PATTERN;

  private InetAddress ip;
  private int maxRetries;
  private Message msg;
  private int port;
  private FailStrategy strategy;
  private String hash;

  public DefaultNetMessage() {

  }

  public DefaultNetMessage(String ip, int port) {
    this.setIp(ip);
    this.setPort(port);
  }

  public DefaultNetMessage(String ip, int port, Message msg) {
    this.setIp(ip);
    this.setPort(port);
    this.setMessage(msg);
  }

  public DefaultNetMessage(InetAddress ip, int port, Message msg) {
    this.ip = ip;
    this.setPort(port);
    this.setMessage(msg);
  }

  public DefaultNetMessage(String ip, int port, int maxRetries) {
    this.setIp(ip);
    this.setPort(port);
    this.setMaxRetries(maxRetries);
  }

  public DefaultNetMessage(String ip, int port, int maxRetries, Message msg) {
    this.setIp(ip);
    this.setPort(port);
    this.setMaxRetries(maxRetries);
    this.setMessage(msg);
  }

  public DefaultNetMessage(String ip, int port, int maxRetries, Message msg, FailStrategy strategy) {
    this.setIp(ip);
    this.setPort(port);
    this.setMaxRetries(maxRetries);
    this.setMessage(msg);
    this.setFailStrategy(strategy);
  }

  @Override
  public void setIp(String ip) {
    try {
      this.ip = InetAddress.getByName(ip);

    } catch (UnknownHostException err) {
      throw new IllegalArgumentException("Net message expect a valid address ip.");
    }
  }

  @Override
  public InetAddress getIp() {
    return this.ip;
  }

  @Override
  public void setPort(int port) {
    if (port < 1023) {
      throw new IllegalArgumentException("Net message expect a valid port. Must be a non reserved port.");
    }
    this.port = port;
  }

  @Override
  public int getPort() {
    return this.port;
  }

  @Override
  public void setMessage(Message message) {
    this.msg = message;

    this.hash = "";
    if (!(this.msg instanceof AckMessage)) {

      this.hash = new String(MD5.digest(String
          .format("%s:%d", this.msg.serialize(), new Date().getTime()).getBytes()));
    }
  }

  @Override
  public Message getMessage() {
    return this.msg;
  }

  @Override
  public String getHash() {
    return this.hash;
  }

  @Override
  public int getMaxRetries() {
    return this.maxRetries;
  }

  @Override
  public void setMaxRetries(int maxRetries) {
    if (maxRetries > -1) {
      this.maxRetries = maxRetries;
    }
  }

  @Override
  public void setFailStrategy(FailStrategy strategy) {
    this.strategy = strategy;
  }

  @Override
  public FailStrategy getFailStrategy() {
    return this.strategy;
  }

  @Override
  public boolean isMaxRetriesReach() {
    return this.maxRetries < 1;
  }

  @Override
  public Message executeFailStrategy() {
    Message msg = null;
    if (this.strategy != null) {
      this.strategy.execute();

      if (this.strategy.produceMessage()) {
        msg = this.strategy.getMessage();
      }
    }
    return msg;
  }

  @SuppressWarnings("unchecked")
  public static NetMessage fromString(String string) {
    DefaultNetMessage message = null;

    if (string != null) {
      Matcher m = NET_MESSAGE_PATTERN.matcher(string);

      if (m.matches()) {
        try {
          Class<? extends Message> clz = (Class<? extends Message>) Class.forName(m.group(1));

          message = new DefaultNetMessage();
          message.setMessage(new Gson().fromJson(m.group(2), clz));

          if (!(message.getMessage() instanceof AckMessage)) {
            message.hash = m.group(3);
          }
        } catch (ClassNotFoundException ok) {
          // Simply return null.
        }
      }
    }
    return message;
  }

  static {
    try {
      MD5 = MessageDigest.getInstance("MD5");
      NET_MESSAGE_PATTERN = Pattern.compile("^\\[([\\w|.]+)]\\((.+)\\)(\\[(.+)])?$");

    } catch (NoSuchAlgorithmException err) {
      throw new RuntimeException("Cannot create net message missing MD5 instance.");
    }
  }
}
