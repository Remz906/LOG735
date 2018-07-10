package arreat.impl.net;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

/**
 * Created by am42010 on 2018-07-04.
 */
public class Receiver implements Runnable {

    private final DatagramSocket socket;
    private final byte[] buffer;

    public Receiver(DatagramSocket socket, byte[] buffer) {
        this.socket = socket;
        this.buffer = buffer;
    }

    @Override
    public void run() {
        try {
            while (true) {
                try {
                    DatagramPacket packet = this.createPacket();

                    this.socket.receive(packet);

                    String value = new String(packet.getData(), 0, packet.getLength());

                    System.out.printf("Received> %s (from %s:%d)\n", value, packet.getAddress(), packet.getPort());

                } catch (IOException e) {
                    e.printStackTrace();

                }
            }
        } catch (Exception ok) {
            // Exception caught due to exiting thread.

        } finally {
            if (this.socket != null && !this.socket.isClosed()) {
                this.socket.close();
            }
        }
    }

    private DatagramPacket createPacket() {
        return new DatagramPacket(this.buffer, this.buffer.length);
    }
}
