package arreat.impl.net;

import java.net.DatagramPacket;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

/**
 * Created by am42010 on 2018-07-04.
 */
public class Receiver implements Runnable {

//    private final DatagramSocket socket;
    private final byte[] buffer;


    private LinkedList<UDPMessage> UDPMessages;

    public Receiver(byte[] buffer) {
//        this.socket = socket;
        this.buffer = buffer;
        this.UDPMessages = new LinkedList<>();
    }

    public LinkedList<UDPMessage> getUDPMessages() {
        return UDPMessages;
    }

    public Receiver setUDPMessages(LinkedList<UDPMessage> UDPMessages) {
        this.UDPMessages = UDPMessages;
        return this;
    }

    @Override
    public void run() {
        try {
            while (true) {
//                TimeUnit.MILLISECONDS.sleep(1);

                DatagramPacket packet = this.createPacket();

                NetSocket.getInstance().receive(packet);

                String value = new String(packet.getData(), 0, packet.getLength());
                UDPMessages.add(new UDPMessage(packet.getAddress().toString(), packet.getPort(), value.trim()));

                System.out.printf("Received> %s (from %s:%d)\n", value, packet.getAddress(), packet.getPort());
            }
        } catch (Exception ok) {
            // Exception caught due to exiting thread.

        } finally {
//            if (this.socket != null && !this.socket.isClosed()) {
//                this.socket.close();
//            }
        }
    }
    private DatagramPacket createPacket() {
        return new DatagramPacket(this.buffer, this.buffer.length);
    }
}
