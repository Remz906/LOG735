import arreat.impl.Client;
import arreat.impl.DataBase;
import arreat.impl.Message;
import arreat.impl.core.NetService;

import java.net.UnknownHostException;
import java.util.Scanner;

/**
 * Created by am42010 on 2018-07-04.
 */
public class Main {

    public static void main(String[] args) throws UnknownHostException {
        if (args.length > 0) {
            switch (args[0]) {
                case "client":
                    DataBase DB;
                    DB = new DataBase();

                    DB.initDBhistoricDiscussion();
                    DB.initDBClientIP();
                    DB.newMessage(new Message("discussion1", 1, "G4", "hello world"));
                    DB.newMessage(new Message("discussion1", 1, "G4", "hello world"));
                    System.out.println(DB.readMessages().size());
                    DB.newClient(new Client("197.565.454.454",49444, "G4" ));
                    DB.readClients();
                case "serveur":

                    break;
                default:
                    break;
            }
        }
        //this is not suppose to happen obviously
        else {
            System.out.println("YOOOOOOOO les arguments sont vide NigA!");
            NetService net = NetService.getInstance();
            Scanner scanner = new Scanner(System.in);
            boolean loop = true;

            while (loop) {
                String value = scanner.nextLine();

                loop = !"exit".equalsIgnoreCase(value);

                if (loop) {
                    net.send("127.0.0.0",8080,value);
                }
            }
        }
    }
}
