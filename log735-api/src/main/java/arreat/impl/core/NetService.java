package arreat.impl.core;

import arreat.api.core.Service;
import arreat.impl.config.NetConfiguration;
import arreat.impl.net.NetSocket;
import arreat.impl.net.Receiver;
import arreat.impl.net.Sender;
import arreat.impl.net.UDPMessage;
import com.google.common.util.concurrent.MoreExecutors;

import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by am42010 on 2018-07-04.
 */
public final class NetService implements Service {

    private static volatile NetService instance;

    private ExecutorService receiverService;
    private Sender sender;
    private byte[] buffer;
    private Receiver receiver;


    private int portNumber = 9080;

    // TODO: Is this really used?
    private  String ipAddress = "127.0.0.1";

    private NetService() throws SocketException {
    }

    public void init () throws SocketException {
        this.buffer = new byte[1024];
        this.receiverService = MoreExecutors.getExitingExecutorService(
                (ThreadPoolExecutor) Executors.newFixedThreadPool(1), 1000, TimeUnit.MILLISECONDS);

        this.receiver = new Receiver(this.buffer);
        this.receiverService.submit(receiver);
        this.sender = new Sender(3);
    }

    public void send(SocketAddress address, String string) {
        this.sender.send(new DatagramPacket(string.getBytes(), string.getBytes().length, address));
    }

    public void send(String ipAdd, int portNb, String string) throws UnknownHostException {
        InetAddress address = InetAddress.getByName(ipAdd);
        this.sender.send(new DatagramPacket(string.getBytes(), string.length(), address, portNumber));
    }

    public UDPMessage receive() {
        return this.receiver.getUDPMessages().pop();
    }

    @Deprecated
    public int getPortNumber() {
        return portNumber;
    }

    @Deprecated
    public NetService setPortNumber(int portNumber) {
        this.portNumber = portNumber;
        return this;
    }

    @Deprecated
    public String getIpAddress() {
        return ipAddress;
    }

    @Deprecated
    public NetService setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
        return this;
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

    @Override
    public void configure() {
        NetConfiguration cfg = ConfigurationProvider.getGlobalConfig().getNetConfiguration();

        this.portNumber = cfg.getReceivingPort();
        NetSocket.getInstance().init(this.portNumber);

        try {
            this.init();
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    @Override
    public EventManager getEventManager() {
        return null;
    }
}
