package client.ui;

import arreat.api.registry.Entry;
import arreat.api.registry.RoomEntry;
import arreat.api.registry.UserEntry;
import arreat.core.registry.Room;
import arreat.core.registry.User;
import arreat.core.service.NetService;
import arreat.core.service.RegistryService;
import client.chat.Message;
import com.google.gson.Gson;
import javafx.geometry.Side;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;

import java.util.Collections;
import java.util.List;

public class ChatTabs extends TabPane {

  private static List<Tab> tabs;

  public ChatTabs() {
    this.setSide(Side.LEFT);
    this.getTabs().add(new JoinTab());
    tabs = this.getTabs();
  }

  public static void createChat(String username) {
    tabs.add(0, new ChatTab(username));
  }

  public void receiveMessage(String chatName, String sender, String message) {
    ChatTab chat = getChat(chatName);

    if (chat == null) {
      chat = new ChatTab(chatName);
      this.getTabs().add(0, chat);
    }
    chat.addMessage(sender, message);
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
    private final Button createButton;

    private JoinTab() {
      GridPane pane = new GridPane();

      this.chatName = new TextField();
      this.password = new PasswordField();

      this.joinButton = new Button("Chat with user");
      this.joinButton.setOnMouseClicked(event -> {
        String recipient = chatName.getText().trim();

        if (!"".equals(recipient) && !chatExist(recipient)) {
          Entry entry = RegistryService.getInstance().getRegistry().getUserByName(recipient);

          if (entry == null) {
            NetService.getInstance()
                .send(RegistryService.getInstance().getRegistry().getMasterOrigin().getAddress(),
                    String.format("USER:GET_BY_USERNAMES:%s",
                        new Gson().toJson(Collections.singleton(recipient))));
          } else {
            addTab(recipient);
          }
          chatName.setText("");
        }
      });

      this.createButton = new Button("Create or Join Chat Room");
      this.createButton.setOnMouseClicked(event -> {
        String chatRoomName = chatName.getText().trim();
        String pwd = password.getText();

        if (!"".equals(chatRoomName) && !pwd.equals("") && !chatExist(chatRoomName)) {
          Entry entry = RegistryService.getInstance().getRegistry().getRoomByName(chatRoomName);

          if (entry == null) {
            NetService.getInstance()
                .send(RegistryService.getInstance().getRegistry().getMasterOrigin().getAddress(),
                    String.format("NODE:AUTH:%s:%s", chatRoomName, pwd));
          } else {
            addTab(chatRoomName);
          }
          chatName.setText("");
          password.setText("");
        }
      });

      pane.add(JOIN_LABEL, 1, 1);
      pane.add(PASSWORD_LABEL, 1, 2);

      pane.add(this.chatName, 2, 1);
      pane.add(this.password, 2, 2);

      pane.add(this.joinButton, 1, 3);
      pane.add(this.createButton, 2, 3);

      this.setContent(pane);
      this.setText("(+)");
      this.setClosable(false);
    }

    private void addTab(String recipient) {
      this.getTabPane().getTabs().add(0, new ChatTabs.ChatTab(recipient));
    }

    static {
      JOIN_LABEL = new Label("Direct Chat or Join a Channel");
      PASSWORD_LABEL = new Label("Password (for chat room)");
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

      pane.add(this.chatTitle, 1, 1, 3, 1);
      pane.add(this.chat, 1, 2, 3, 4);
      pane.add(this.input, 1, 6, 2, 1);
      pane.add(this.inputButton, 3, 6);

      this.setContent(pane);
      this.setText(recipient);
    }

    private void sendMessage() {
      String message = this.input.getText().trim();
      if (!"".equals(message)) {
        Entry target = RegistryService.getUserByName(this.getText());

        Message chatMessage = new Message();

        chatMessage.setRecipient(this.getText());
        chatMessage.setBody(message);

        if (target != null) {

          chatMessage
              .setSender(RegistryService.getSelf().getName());

          NetService.getInstance().send(target.getAddress(), chatMessage.toString());

        } else {
          target = RegistryService.getRoomByName(this.getText());
          if (target != null) {
            for (UserEntry member : ((RoomEntry) target).getMembers()) {
              if (!RegistryService.isSelf(member) && !member.getName()
                  .equals(chatMessage.getSender())) {
                NetService.getInstance()
                    .send(member.getAddress(), chatMessage.toString());
              }
            }
          }
        }
        this.addMessage("me", message);
        this.input.setText("");
      }
    }

    private void addMessage(String writer, String message) {
      this.chat.appendText(String.format("[%s] %s\n\n", writer, message));
    }
  }
}
