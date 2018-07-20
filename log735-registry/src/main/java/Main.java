import server.Server;

import java.net.UnknownHostException;

public class Main {
    public static void main(String[] args) {
        Server server;
        switch (args.length){
            case 1:
                server = new Server(Integer.valueOf(args[0]));
                break;
            case 2:
                server = new Server(Integer.valueOf(args[0]),args[1]);
                break;
            default:
                server = new Server();
                break;
        }

        boolean shutdown = false;
        server.run();
        while(true){

        }
    }
}
