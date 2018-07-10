package arreat.impl.net;

import arreat.api.message.Body;
import arreat.api.message.Message;
import arreat.api.net.Packet;
import arreat.api.net.PacketFactory;
import com.google.gson.Gson;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.DatagramPacket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public final class NetPacketFactory implements PacketFactory {

    private static final MessageDigest MD5_DIGEST;
    private static final Set<Class<? extends Message>> MESSAGE_WITH_ACKNOWLEGDMENT;

    @Override
    public Packet make(Message message) {
        Packet packet = new NetPacket();
        packet.setMessage(message);

        if (this.mustAcknowledge(message)) {
            packet.setAcknowledgmentHash(this.hash(message));
        }

        return packet;
    }

    @Override
    public List<Packet> make(Message message, int split) {
        List<Packet> packets = new ArrayList<>();
        Packet basePacket = this.make(message);

        for (int i = 0; i < split; i++) {
            Packet packet = new NetPacket();
        }
        return packets;
    }

    @Override
    public Packet make(DatagramPacket packet) {
        Gson gson = new Gson();

        NetPacket.NetPacketBody netBody =
                gson.fromJson(new String(packet.getData()), NetPacket.NetPacketBody.class);

        if (netBody.getBody() == null || netBody.getBodyImplementationName() == null
                || netBody.getMessageImplementationName() == null) {

            throw new IllegalArgumentException("Unknown NetBody found.");
        }

        Message message = makeMessage(netBody.getMessageImplementationName());
        message.setBody((Body) gson.fromJson(netBody.getBody(), this.getClass(netBody.getBodyImplementationName())));

        NetPacket netPacket = new NetPacket();

        netPacket.setAcknowledgmentHash(netPacket.getAcknowledgmentHash());
        netPacket.setFragmentNumber(netBody.getFragmentNumber());
        netPacket.setMessage(message);

        return netPacket;
    }

    private Class<?> getClass(String bodyImplementationName) {
        Class<?> clz;

        try {
            clz = Class.forName(bodyImplementationName);

        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("Unknown implementation of body or message interface.");
        }
        return clz;
    }

    private Message makeMessage(String messageImplementationName) {
        Class<?> messageClass = this.getClass(messageImplementationName);

        Message message = null;

        try {
            Constructor ctor = messageClass.getDeclaredConstructor();
            ctor.setAccessible(true);

            message = (Message) ctor.newInstance();

        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException
                | InstantiationException ok) {
            // Should not be thrown, otherwise we return null.
        }

        return message;
    }

    private String hash(Message message) {
        String string = message.getTarget().getKey() + new Date();

        return new String(MD5_DIGEST.digest(string.getBytes()));
    }

    private boolean mustAcknowledge(Message message) {
        return MESSAGE_WITH_ACKNOWLEGDMENT.contains(message.getClass());
    }

    static {
        try {
            MD5_DIGEST = MessageDigest.getInstance("MD5");

            MESSAGE_WITH_ACKNOWLEGDMENT = new HashSet<>();

        } catch (NoSuchAlgorithmException err) {
            throw new RuntimeException(err);
        }
    }
}
