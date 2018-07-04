package arreat.api.net;

import arreat.api.message.AcknowledgementMessage;
import arreat.api.message.Message;
import arreat.api.registry.RegistryEntry;
import com.google.gson.Gson;
import org.apache.commons.codec.digest.DigestUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;

public class MessageProcessor {

    private static final char SPACE = ' ';

    private final Charset charSet;
    private final int packetSize;
    private final Gson parser;

    public MessageProcessor(Charset charSet, int packetSize) {
        this.charSet = charSet;
        this.packetSize = packetSize;
        this.parser = new Gson();
    }

    public Message acknowledge(NetMessage netMessage) {
        if (netMessage == null || netMessage.getHash() == null) {
            throw new IllegalArgumentException("Unable to acknowledge, Net Message is null or without acknowledgment.");
        }
        AcknowledgementMessage acknowledgement = new AcknowledgementMessage();

        acknowledgement.setContent(netMessage.getHash());
        acknowledgement.setReceivers(new String[]{netMessage.getSender()});
        acknowledgement.setSender(netMessage.getRecipient());

        return acknowledgement;
    }

    public DatagramPacket prepare(final NetMessage netMessage, RegistryEntry entry) {
        if (netMessage == null) {
            throw new IllegalArgumentException("Unable to prepare, Net Message cannot be null.");
        }
        if (entry == null) {
            throw new IllegalArgumentException("Unable to prepare, Registry Entry cannot be null.");
        }
        return new DatagramPacket(this.parse(netMessage), this.packetSize, this.parse(entry));
    }

    public Message unwrap(NetMessage netMessage) {
        if (netMessage == null) {
            throw new IllegalArgumentException("Unable to unwrap, Net Message cannot be null.");
        }
        Message message = this.create(netMessage.getWrapped());

        message.setContent(netMessage.getContent());
        message.setReceivers(new String[] {netMessage.getRecipient()});
        message.setSender(netMessage.getSender());

        return message;
    }

    public Set<NetMessage> wrap(Message message) {
        if (message == null) {
            throw new IllegalArgumentException("Unable to wrap, Message cannot be null.");
        }
        return Arrays.stream(message.getReceivers()).map(e-> create(message, e)).collect(Collectors.toSet());
    }

    private Message create(String className) {
        if (className == null) {
            throw new IllegalArgumentException("Unable to unwrap, Message type is null.");
        }
        Message message = null;

        try {
            Class<?> clz = Class.forName(className);
            Constructor<?> ctor = clz.getDeclaredConstructor();
            ctor.setAccessible(true);
            message = (Message) ctor.newInstance();

        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("Unable to unwrap, Unknown Message type.");

        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("Unable to unwrap, Message is missing default constructor.");

        } catch (IllegalAccessException e) {
            // Cannot happen, we override the encapsulation.

        } catch (InstantiationException | InvocationTargetException e) {
            throw new RuntimeException("Failed to create message, something went wrong.");

        } catch (ClassCastException e) {
            throw new IllegalArgumentException("Unable to unwrap, Message type is wrong.");
        }

        return message;
    }

    private static NetMessage create(Message message, String recipient) {
        NetMessage netMessage = new NetMessage();

        netMessage.setContent(message.getContent());
        netMessage.setRecipient(recipient);
        netMessage.setSender(message.getSender());

        if (!(message instanceof AcknowledgementMessage)) {
            netMessage.setHash(new String(DigestUtils.sha1(String.format("%s$%d", message.getSender(), new Date().getTime()))));
        }
        return netMessage;
    }

    private byte[] parse(NetMessage netMessage) {
        StringBuilder preparedMessage = new StringBuilder(this.parser.toJson(netMessage));

        if (preparedMessage.length() > this.packetSize) {
            throw new IllegalArgumentException("Unable to parse, Net Message too long.");
        }

        if (preparedMessage.length() < this.packetSize) {
            int charToFill = this.packetSize - preparedMessage.length();

            for (int i = 0; i < charToFill; i++) {
                preparedMessage.append(SPACE);
            }
        }
        return preparedMessage.toString().getBytes(this.charSet);
    }

    private SocketAddress parse(RegistryEntry entry) {
        return new InetSocketAddress(entry.getHost(), entry.getPort());
    }
}
