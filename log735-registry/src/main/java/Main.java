import java.util.Scanner;
import server.Server;

public class Main {

  public static void main(String[] args) {
    Server server = new Server();

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
