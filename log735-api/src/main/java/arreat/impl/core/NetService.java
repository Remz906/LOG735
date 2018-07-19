package arreat.impl.core;

import arreat.impl.net.Receiver;
import arreat.impl.net.Sender;
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
    private static final int PORT_NUMBER = 1337;
    private static final String IP_ADDRESS = "127.0.0.1";

    private NetService() throws SocketException {
        this.buffer = new byte[1024];
        this.receiverService = MoreExecutors.getExitingExecutorService(
                (ThreadPoolExecutor) Executors.newFixedThreadPool(1), 1000, TimeUnit.MILLISECONDS);

        this.receiverService.submit(new Receiver(new DatagramSocket(PORT_NUMBER), this.buffer));
        this.sender = new Sender(3);
    }

    public void send(String ipAdd, int portNb, String string) throws UnknownHostException {
        InetAddress address = InetAddress.getByName(ipAdd);
        this.sender.send(new DatagramPacket(string.getBytes(), string.length(), address, PORT_NUMBER));
    }

    public String receive() throws UnknownHostException {
        return null;
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
