package client.ui;

import arreat.api.registry.UserEntry;
import arreat.core.service.NetService;
import arreat.core.service.RegistryService;
import com.google.gson.Gson;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

public class LoginScene extends Scene {

    public LoginScene() {
        super(new LoginPane());
    }

    private static class LoginPane extends GridPane {

        private static final Label USERNAME_LABEL;
        private static final Label PASSWORD_LABEL;

        private final Button loginButton;
        private final Button registerButton;
        private final PasswordField password;
        private final TextField username;

        private LoginPane() {
            //Setting the padding
            this.setPadding(new Insets(10, 10, 10, 10));

            this.username = new TextField();
            this.password = new PasswordField();

            this.loginButton = new Button("Login");
            this.loginButton.setOnMouseClicked(event -> {
                NetService.getInstance().send(
                        RegistryService.getInstance().getRegistry().getMasterOrigin().getAddress(),
                        String.format("USER:AUTH:%s:%s", username.getText(), password.getText()));

                username.setText("");
                password.setText("");
            });

            this.registerButton = new Button("Register");
            this.registerButton.setOnMouseClicked(event -> {
                UserEntry user = RegistryService.createUser();

                user.setName(username.getText());
                user.setPassword(password.getText());

                NetService.getInstance().send(
                        RegistryService.getInstance().getRegistry().getMasterOrigin().getAddress(),
                        String.format("USER:ADD:%s", new Gson().toJson(user)));

                username.setText("");
                password.setText("");
            });

            this.add(USERNAME_LABEL, 1, 1);
            this.add(this.username, 2, 1);

            this.add(PASSWORD_LABEL, 1, 2);
            this.add(this.password, 2, 2);

            this.add(this.loginButton, 1, 3);
            this.add(this.registerButton, 2, 3);
        }

        static {
            USERNAME_LABEL = new Label("Username");
            PASSWORD_LABEL = new Label("Password");
        }
    }
}
