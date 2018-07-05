package arreat.api.net;

import com.google.common.util.concurrent.MoreExecutors;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by am42010 on 2018-07-04.
 */
public class Sender {

    private final ExecutorService pool;

    public Sender(int poolSize) {
        this.pool = MoreExecutors.getExitingExecutorService(
                (ThreadPoolExecutor) Executors.newFixedThreadPool(poolSize), 1000, TimeUnit.MILLISECONDS);
    }

    public void send(DatagramPacket packet) {
        this.pool.submit(new SendingWorker(packet));
    }

    private static class SendingWorker implements Runnable {

        private final DatagramPacket packet;

        private SendingWorker(DatagramPacket packet) {
            this.packet = packet;
        }

        @Override
        public void run() {
            try {
                DatagramSocket socket = new DatagramSocket();
                socket.send(packet);
                socket.close();

            } catch (SocketException e) {
                // Caused by the socket that fail to be created.
                e.printStackTrace();

            } catch (IOException e) {
                // Caused by send if it fails.

                e.printStackTrace();
            }
        }
    }
}
