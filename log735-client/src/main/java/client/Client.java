package client;

import arreat.api.registry.Entry;
import arreat.api.registry.OriginEntry;
import arreat.api.registry.RoomEntry;
import arreat.api.registry.UserEntry;
import arreat.core.registry.Origin;
import arreat.core.registry.Room;
import arreat.core.registry.User;
import arreat.core.service.NetService;
import arreat.core.service.RegistryService;
import arreat.core.net.UDPMessage;
import client.chat.Message;
import client.ui.ChatScene;
import client.ui.ChatTabs;
import client.ui.LoginScene;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Client extends Application {

  private static Stage primaryStage;

  private static boolean stopped = false;

  public static void main(String[] args) {
    // Service Initialization.
    NetService.getInstance().configure();
    RegistryService.getInstance().configure();

    // Start the service.
    new MessageReceiver().start();

    Application.launch(args);
  }

  @Override
  public void start(Stage stage) {
    primaryStage = stage;
    switchScene(new LoginScene(), "Login");
  }

  @Override
  public void stop() throws Exception {
    super.stop();
    stopped = true;
  }

  public static void switchScene(Scene scene, String title) {
    primaryStage.setTitle(title);
    primaryStage.setScene(scene);

    primaryStage.show();
  }

  private static class MessageReceiver extends Thread {

    private static final Pattern MSG_PATTERN;

    private MessageReceiver() {
      super(() -> {
        while (!stopped) {
          try {
            UDPMessage udpMessage = NetService.getInstance().receive();

            if (udpMessage != null) {
              Matcher msg = MSG_PATTERN.matcher(udpMessage.getMsg());

              if (msg.matches()) {
                switch (msg.group(1)) {

                  // Message from origin registry.
                  case "USER":
                    msg = MSG_PATTERN.matcher(msg.group(2));

                    if (msg.matches()) {
                      switch (msg.group(1)) {

                        // Manage Response for auth/login.
                        case "AUTH":
                          if (!"fail".equalsIgnoreCase(msg.group(2))) {
                            UserEntry user = RegistryService.userFromJson(msg.group(2));

                            RegistryService.getInstance().getRegistry().save(user);
                            RegistryService.getInstance().getRegistry().setSelf(user);

                            final String username = user.getName();
                            Platform.runLater(() -> switchScene(new ChatScene(),
                                String.format("Chat - %s", username)));
                          }
                          break;

                        // Manage the refresh info response.
                        case "GET_BY_USERNAMES":
                          List<? extends UserEntry> users = RegistryService.userListFromJson(msg.group(2));

                          for (UserEntry user : users) {
                            RegistryService.getInstance().getRegistry().save(user);

                            String username = user.getName();

                            if (!ChatTabs.chatExist(user.getName())) {
                              Platform.runLater(() -> ChatTabs.createChat(username));
                            }
                          }
                          break;

                        case "UM":
                          OriginEntry master = RegistryService.originFromJson(msg.group(2));

                          RegistryService.getInstance().getRegistry().setMasterOrigin(master);
                          break;
                      }
                    }

                  case "NODE":
                    msg = MSG_PATTERN.matcher(msg.group(2));

                    if (msg.matches()) {
                      switch (msg.group(1)) {
                        case "AUTH":
                          if (!"fail".equalsIgnoreCase(msg.group(2))) {
                            UserEntry entry = RegistryService.userFromJson(msg.group(2));
                            RegistryService.getInstance().getRegistry().save(entry);

                            String roomName = entry.getName();

                            if (!ChatTabs.chatExist(roomName)) {
                              Platform.runLater(() -> ChatTabs.createChat(roomName));
                            }
                          }
                          // JSON CLIENT.
                          break;

                        case "CREATED":
                          RoomEntry room = RegistryService.createRoom();

                          String roomName = msg.group(2);

                          room.setName(roomName);
                          room.setOwner((UserEntry) RegistryService.getSelf());

                          RegistryService.getInstance().getRegistry().save(room);

                          if (!ChatTabs.chatExist(roomName)) {
                            Platform.runLater(() -> ChatTabs.createChat(roomName));
                          }
                          break;

                        default:
                          room = RegistryService.getInstance().getRegistry()
                              .getRoomByName(msg.group(1));

                          if (room != null) {
                            UserEntry member = RegistryService.userFromJson(msg.group(2));

                            room.add(member);

                            RegistryService.getInstance().getRegistry().save(room);
                          }
                          break;
                      }
                    }
                    break;

                  // Manage chat messages.
                  case "CMSG":
                    Message chatMessage = Message.fromJson(msg.group(2));

                    // If it's not a message for us, it means it's a chat room.
                    if (!RegistryService.getInstance().getRegistry()
                        .isSelf(chatMessage.getRecipient())) {
                      Entry entry = RegistryService.getInstance().getRegistry()
                          .getRoomByName(chatMessage.getRecipient());

                      // If not found (means null and non instanceof, it means we're not
                      // the master node.
                      if (entry instanceof Room) {
                        for (UserEntry member : ((Room) entry).getMembers()) {
                          if (!RegistryService.getInstance().getRegistry().isSelf(member) && !member
                              .getName().equals(chatMessage.getSender())) {
                            Entry e = RegistryService.getInstance().getRegistry()
                                .getUserByName(member.getName());
                            NetService.getInstance().send(e.getAddress(), msg.group(0));
                          }
                        }
                        // Make sure we have an entry for the chat room if not we create it.
                      } else if (entry == null) {
                        entry = new User();
                        ((User) entry).setName(chatMessage.getRecipient());
                        ((User) entry).setIp(udpMessage.getIp());
                        ((User) entry).setPort(udpMessage.getPort());

                        RegistryService.getInstance().getRegistry().save(entry);
                      }

                      // Detach, this will be run the FX Application thread otherwise we can't modify the UI.
                      Platform.runLater(() -> ChatScene
                          .receiveMessage(chatMessage.getRecipient(), chatMessage.getSender(),
                              chatMessage.getBody()));

                      // Simple one to one message.
                    } else {
                      Entry entry = RegistryService.getInstance().getRegistry()
                          .getUserByName(chatMessage.getSender());

                      // Update the information of the sender.
                      if (entry instanceof User) {
                        ((User) entry).setPort(udpMessage.getPort());
                        ((User) entry).setIp(udpMessage.getIp());

                        // Create a new entry for the sender.
                      } else if (entry == null) {
                        entry = new User();
                        entry.setName(chatMessage.getSender());
                        ((User) entry).setPort(udpMessage.getPort());
                        ((User) entry).setIp(udpMessage.getIp());
                      }
                      RegistryService.getInstance().getRegistry().save(entry);

                      // Detach, this will be run the FX Application thread otherwise we can't modify the UI.
                      Platform.runLater(() -> ChatScene
                          .receiveMessage(chatMessage.getSender(), chatMessage.getSender(),
                              chatMessage.getBody()));
                    }
                    break;
                  default:
                    // No default.
                }
              }
            }
          } catch (Exception e) {

          }
        }
      });
    }

    static {
      MSG_PATTERN = Pattern.compile("(\\w+):(.+)");
    }
  }
}
