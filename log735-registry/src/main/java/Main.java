import arreat.impl.core.NetService;
import server.Pair;
import server.Server;

import java.net.Inet4Address;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.*;

public class Main {

    private static final String LOCAL_HOST = "127.0.0.0";

    private static List<Pair<String, Integer>> listOfServers = new LinkedList<>();


    public static void main(String[] args) throws SocketException {
        listOfServers.add(new Pair<>("127.0.0.1", 9080));
        listOfServers.add(new Pair<>("127.0.0.1" , 9081));
        listOfServers.add(new Pair<>("127.0.0.1", 9082));
        NetService.getInstance().configure();

        Server server = null;
//        switch(args[0]){
//            case "1":
//                server = new Server(9080,LOCAL_HOST, listOfServers);
//                break;
//            case "2":
//                server = new Server(9081, LOCAL_HOST, listOfServers);
//                break;
//            case "3":
//                server = new Server(9081, LOCAL_HOST, listOfServers);
//                break;
//        }
        server = new Server(9082,"127.0.0.1", listOfServers);

        if (server != null) {
            boolean shutdown = false;
            new Thread(server).start();
            Scanner sc = new Scanner(System.in);


            while (!shutdown) {
                if ("exit".equals(sc.nextLine())) {
                    server.requestShutdown();
                    shutdown = true;
                }
            }
            System.exit(0);
        }
    }
}
