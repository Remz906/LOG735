package client.ui;

import arreat.impl.core.NetService;
import client.Client;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

import java.net.UnknownHostException;

public class LoginScene extends Scene {

    public LoginScene() {
        super(new LoginPane());
    }

    private static class LoginPane extends GridPane {

        private static final Label USERNAME_LABEL;
        private static final Label PASSWORD_LABEL;

        private final Button loginButton;
        private final PasswordField password;
        private final TextField username;

        private LoginPane() {
            //Setting the padding
            this.setPadding(new Insets(10, 10, 10, 10));

            this.loginButton = new Button("Login");
            this.username = new TextField();
            this.password = new PasswordField();

            this.loginButton.setOnMouseClicked(event -> {
                if (login()) {
                    switchScene();
                }
            });

            this.add(USERNAME_LABEL, 1, 1);
            this.add(PASSWORD_LABEL, 1, 2);
            this.add(this.loginButton, 1, 3);

            this.add(this.username, 2, 1);
            this.add(this.password, 2, 2);
        }

        private boolean login() {
            // TODO: Implement.

//            NetService netService = NetService.getInstance();
//            try {
//                netService.send("", 0,
//                        String.format("UF:AUTH:%s:%s", username.getText(), password.getText()));
//
//            } catch (UnknownHostException e) {
//                // DO SOMETHING !?
//            }
            return true;
        }

        private void switchScene() {
            Client.switchScene(new ChatScene(), "Chat");
        }

        static {
            USERNAME_LABEL = new Label("Username");
            PASSWORD_LABEL = new Label("Password");
        }
    }
}
