import arreat.api.core.NetService;

import java.net.UnknownHostException;

/**
 * Created by am42010 on 2018-07-04.
 */
public class Main {

    public static void main(String[] args) throws UnknownHostException {
        NetService net = NetService.getInstance();

        net.send("Allo");
        net.send("Comment ça va?");
        net.send("Bye bye");
    }
}
