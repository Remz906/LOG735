import arreat.db.DatabaseH2;
import arreat.core.service.NetService;

import java.net.UnknownHostException;
import java.sql.SQLException;
import java.util.Scanner;

/**
 * Created by am42010 on 2018-07-04.
 */
public class Main {

    public static void main(String[] args) throws UnknownHostException {
        if (args.length > 0) {
            switch (args[0]) {
                case "client":
                    DatabaseH2 DB = new DatabaseH2();

                    try {
                        DB.initDBhistoricDiscussion();

//                    db.initDBClientIP();
//                    db.newMessage(new Message("discussion1", 1, "G4", "hello world"));
//                    db.newMessage(new Message("discussion1", 1, "G4", "hello world"));
//                    System.out.println(db.readMessages().size());
//                    db.newClient(new Client("197.565.454.454",49444, "G4" ));
//                    db.readClients();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
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
