package client;

import arreat.impl.core.NetService;
import arreat.impl.net.UDPMessage;
import client.ui.ChatScene;
import client.ui.LoginScene;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.SocketException;

public class Client extends Application {

    private static Stage primaryStage;

    public static void main(String[] args) throws SocketException {
        // Service Initialization.
        NetService netService = NetService.getInstance();

        netService.setIpAddress("127.0.0.1");
        netService.setPortNumber(1337);

        netService.init();

        // Start the service.
        new MessageReceiver().start();

        Application.launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage  = stage;
        switchScene(new LoginScene(), "Login");
    }

    public static void switchScene(Scene scene, String title) {
        primaryStage.setTitle(title);
        primaryStage.setScene(scene);

        primaryStage.show();
    }

    private static class MessageReceiver extends Thread {

        private MessageReceiver() {
            super(() -> {
                while(true) {
                    NetService netService = NetService.getInstance();

                    UDPMessage udpMessage = netService.receive();

                    // TODO: Implement stuff....

                    // Case it's a login success.

                    // Case it's a chat message.
                    if (false && primaryStage != null && primaryStage.getScene() instanceof ChatScene) {
//                        ChatScene.receiveMessage();
                    }
                }
            });
        }
    }
}
