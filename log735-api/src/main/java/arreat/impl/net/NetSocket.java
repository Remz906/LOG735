package arreat.impl.net;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.LinkedList;
import java.util.Queue;

public final class NetSocket {

    private static volatile NetSocket instance;

    private DatagramChannel channel;

    private NetSocket() {
        try {
            this.channel = DatagramChannel.open();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized void init(int port) {
        try {
            this.channel.socket().bind(new InetSocketAddress(port));

        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
    }

    public void send(DatagramPacket packet) {
        ByteBuffer buf = ByteBuffer.allocate(packet.getLength());
        buf.clear();
        buf.put(packet.getData());
        buf.flip();

        try {
            this.channel.send(buf, packet.getSocketAddress());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void receive(DatagramPacket packet) {
        ByteBuffer buf = ByteBuffer.allocate(packet.getLength());
        buf.clear();

        try {
            packet.setSocketAddress(channel.receive(buf));
            packet.setData(buf.array());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static synchronized NetSocket getInstance() {
        return instance;
    }

    static {
        synchronized (NetSocket.class) {
            NetSocket socket = instance;

            if (socket == null) {
                instance = new NetSocket();
            }
        }
    }
}
