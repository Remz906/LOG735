import arreat.api.core.NetService;

import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

/**
 * Created by am42010 on 2018-07-04.
 */
public class Main {

    public static void main(String[] args) throws UnknownHostException {

        if (args.length > 0) {
            switch (args[0]) {
                case "client":
                    try {
                        try {
                            Class.forName("org.h2.Driver");
                            Connection connection = DriverManager.getConnection("jdbc:h2:~/test", "test", "test");
                            Statement statement = connection.createStatement();
                            statement.execute("CREATE TABLE lapluie(NAME VARCHAR(20))");
                            System.out.println("table");
                            connection.close();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                    break;
                case "serveur":

                    break;
                default:
                    break;
            }
        }
        //this is not suppose to happen obviously
        else {
            System.out.println("YOOOOOOOO les arguments sont vide NigA!");
        }


        NetService net = NetService.getInstance();

        Scanner scanner = new Scanner(System.in);
        boolean loop = true;

        while (loop) {
            String value = scanner.next();

            loop = !"exit".equalsIgnoreCase(value);

            if (loop) {
                net.send(value);
            }
        }
    }
}
