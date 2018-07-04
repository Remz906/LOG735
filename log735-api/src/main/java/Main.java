import arreat.api.core.NetService;

import java.net.UnknownHostException;
import java.util.Scanner;

/**
 * Created by am42010 on 2018-07-04.
 */
public class Main {

    public static void main(String[] args) throws UnknownHostException {
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
