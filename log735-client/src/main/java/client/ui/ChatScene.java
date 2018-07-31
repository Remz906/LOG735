package client.ui;

import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;

public class ChatScene extends Scene {

    private static ChatTabs chatTabs;

    public ChatScene() {
        super(new ChatPane());
    }

    private static class ChatPane extends BorderPane {

        private ChatPane() {
            //Setting the padding
//            this.setPadding(new Insets(10, 10, 10, 10));
            chatTabs = new ChatTabs();

            this.setCenter(chatTabs);
        }
    }

    public static void receiveMessage(String sender, String message) {
        if (chatTabs != null) {
            chatTabs.receiveMessage(sender, message);
        }
    }
}
