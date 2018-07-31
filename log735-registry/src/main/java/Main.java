import server.Pair;
import server.Server;

import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.*;

public class Main {

    static private String LOCAL_HOST = "127.0.0.0";
    static private List<Pair<String, Integer>> listOfServers = new LinkedList<>();
    public static void main(String[] args) {
        listOfServers.add(new Pair("127.0.0.1", 9080));
        listOfServers.add(new Pair("127.0.0.1", 9081));
        listOfServers.add(new Pair ("127.0.0.1", 9082));

        Server server = null;
        switch(args[0]){
            case "1":
                server = new Server(9080,LOCAL_HOST, listOfServers);
                break;
            case "2":
                server = new Server(9081, LOCAL_HOST, listOfServers);
                break;
            case "3":
                server = new Server(9081, LOCAL_HOST, listOfServers);
                break;
        }



        boolean shutdown = false;
        server.run();
        while(true);

    }
}
