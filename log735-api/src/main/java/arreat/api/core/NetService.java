package arreat.api.core;

import arreat.api.net.Receiver;
import arreat.api.net.Sender;
import com.google.common.util.concurrent.MoreExecutors;

import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by am42010 on 2018-07-04.
 */
public final class NetService {

    private static volatile NetService instance;

    private final ExecutorService receiverService;
    private final Sender sender;
    private final byte[] buffer;

    private NetService() throws SocketException {
        this.buffer = new byte[1024];
        this.receiverService = MoreExecutors.getExitingExecutorService(
                (ThreadPoolExecutor) Executors.newFixedThreadPool(1), 1000, TimeUnit.MILLISECONDS);

        this.receiverService.submit(new Receiver(new DatagramSocket(1337), this.buffer));
        this.sender = new Sender(3);
    }

    public void send(String string) throws UnknownHostException {
        InetAddress address = InetAddress.getByName("10.196.121.172");
        this.sender.send(new DatagramPacket(string.getBytes(), string.length(), address, 1337));
    }

    public static synchronized NetService getInstance() {
        return instance;
    }

    static {
        synchronized (NetService.class) {
            NetService service = instance;
            if (service == null) {
                try {
                    instance = new NetService();

                } catch (SocketException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
