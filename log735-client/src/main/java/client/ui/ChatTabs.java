package client.ui;

import javafx.geometry.Side;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;

import java.util.List;

public class ChatTabs extends TabPane {

    private static List<Tab> tabs;

    public ChatTabs() {
        this.setSide(Side.LEFT);
        this.getTabs().add(new JoinTab());
        tabs = this.getTabs();
    }

    public static void receiveMessage(String sender, String message) {
        ChatTab chat = getChat(sender);

        if (chat != null) {
            chat.addMessage(sender, message);
        }
    }

    public static ChatTab getChat(String name) {
        Tab tab = null;
        for (Tab t : tabs) {
            if (t.getText().equals(name)) {
                tab = t;
                break;
            }
        }
        return !(tab instanceof ChatTab) ? null : (ChatTab) tab;
    }

    public static boolean chatExist(String name) {
        return getChat(name) != null;
    }

    private static class JoinTab extends Tab {

        private static final Label JOIN_LABEL;
        private static final Label PASSWORD_LABEL;

        private final TextField chatName;
        private final PasswordField password;

        private final Button joinButton;

        private JoinTab() {
            GridPane pane = new GridPane();

            this.chatName = new TextField();
            this.password = new PasswordField();

            this.joinButton = new Button("Join");
            this.joinButton.setOnMouseClicked(event -> {
                String recipient = chatName.getText().trim();

                if (!"".equals(recipient) && !chatExist(recipient) && join(recipient)) {
                    addTab(recipient);
                    chatName.setText("");
                }
            });

            pane.add(JOIN_LABEL, 1, 1);
            pane.add(PASSWORD_LABEL, 1, 2);

            pane.add(this.chatName, 2, 1);
            pane.add(this.password, 2, 2);

            pane.add(this.joinButton, 1,3,2,1);

            this.setContent(pane);
            this.setText("(+)");
            this.setClosable(false);
        }

        private boolean join(String recipient) {
            // TODO: Implement join chat.
            return true;
        }

        private void addTab(String recipient) {
            this.getTabPane().getTabs().add(0, new ChatTabs.ChatTab(recipient));
        }

        static {
            JOIN_LABEL = new Label("Direct Chat or Join a Channel");
            PASSWORD_LABEL = new Label("Password (optional)");
        }
    }

    private static class ChatTab extends Tab {

        private final Label chatTitle;

        private final TextArea chat;
        private final TextField input;
        private final Button inputButton;

        private ChatTab(String recipient) {
            GridPane pane = new GridPane();

            this.chat = new TextArea();
            this.chat.setEditable(false);

            this.chatTitle = new Label(String.format("Discussion with %s", recipient));

            this.input = new TextField();
            this.input.setOnKeyReleased(event -> {
                if (KeyCode.ENTER.equals(event.getCode())) {
                    sendMessage();
                }
            });

            this.inputButton = new Button("Send");
            this.inputButton.setOnMouseClicked(event -> sendMessage());

            pane.add(this.chatTitle, 1,1,3,1);
            pane.add(this.chat, 1,2,3,4);
            pane.add(this.input, 1,6,2,1);
            pane.add(this.inputButton, 3,6);

            this.setContent(pane);
            this.setText(recipient);
        }

        private void sendMessage() {
            String message = this.input.getText().trim();
            if (!"".equals(message)) {
                // TODO: Implement send.
                this.addMessage("me", message);
                this.input.setText("");
            }
        }

        private void addMessage(String writer, String message) {
            this.chat.appendText(String.format("[%s] %s\n\n", writer, message));
        }
    }
}
