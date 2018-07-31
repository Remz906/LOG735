package client.ui;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;

public class ChatScene extends Scene {

    public ChatScene() {
        super(new ChatPane());
    }

    private static class ChatPane extends BorderPane {

        private ChatPane() {
            //Setting the padding
            this.setPadding(new Insets(10, 10, 10, 10));

            this.setCenter(new ChatTabs());
        }
    }
}
