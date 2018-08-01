package client;

import arreat.api.registry.Registry;
import arreat.api.registry.RegistryEntry;
import arreat.impl.core.NetService;
import arreat.impl.core.RegistryService;
import arreat.impl.net.UDPMessage;
import arreat.impl.registry.BaseEntry;
import arreat.impl.registry.RoomBaseEntry;
import client.chat.Message;
import client.ui.ChatScene;
import client.ui.LoginScene;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.SocketException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Client extends Application {

    private static Stage primaryStage;

    private static boolean stopped = false;

    public static void main(String[] args) throws SocketException {
        // Service Initialization.
        NetService.getInstance().configure();
        RegistryService.getInstance().configure();

        // TODO: Remove once test is done.
//        Registry reg = RegistryService.getInstance().getRegistry();
//        BaseEntry testUser = new BaseEntry();
//        testUser.setKey("@test01");
//        testUser.setNetAddress("127.0.0.1");
//        testUser.setPort(1337);
//
//        reg.setSelf(testUser);
//        reg.save(testUser);
//
//        testUser = new BaseEntry();
//        testUser.setKey("@test02");
//        testUser.setNetAddress("127.0.0.1");
//        testUser.setPort(1338);

//        reg.setSelf(testUser);
//        reg.save(testUser);
////
//        testUser = new BaseEntry();
//        testUser.setKey("#test");
//        testUser.setNetAddress("127.0.0.1");
//        testUser.setPort(1337);
//        reg.save(testUser);
//
//        RoomBaseEntry testRoom = new RoomBaseEntry();
//        testRoom.setKey("#test");
//        testRoom.getMembers().add("@test01");
//        testRoom.getMembers().add("@test02");
//        reg.save(testRoom);

        // Start the service.
        new MessageReceiver().start();

        Application.launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage  = stage;
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
                while(!stopped) {
                    try {
                        UDPMessage udpMessage = NetService.getInstance().receive();

                        Matcher msg = MSG_PATTERN.matcher(udpMessage.getMsg());

                        // TODO: Implement stuff....
                        if (msg.matches()) {
                            switch (msg.group(1)) {

                                // Message from remote registry.
                                case "USER":
                                    msg = MSG_PATTERN.matcher(msg.group(2));

                                    if (msg.matches()) {
                                        switch(msg.group(1)) {

                                            // Manage Response for auth/login.
                                            case "AUTH":
                                                if (!"fail".equalsIgnoreCase(msg.group(2))) {
                                                    arreat.db.Client c = arreat.db.Client.fromJson(msg.group(2));

                                                    RegistryEntry entry = RegistryService.getInstance().getRegistry().get(c.getPseudo());

                                                    if (entry == null) {
                                                        entry = new BaseEntry();
                                                        ((BaseEntry) entry).setKey(c.getPseudo());

                                                    }
                                                    ((BaseEntry) entry).setNetAddress(c.getIp());
                                                    ((BaseEntry) entry).setPort(c.getPort());
                                                    RegistryService.getInstance().getRegistry().setSelf(entry);
                                                }
                                                break;
                                        }
                                    }

                                // Manage chat messages.
                                case "CMSG":
                                    Message chatMessage = Message.fromJson(msg.group(2));

                                    // If it's not a message for us, it means it's a chat room.
                                    if (!RegistryService.getInstance().getRegistry().isSelf(chatMessage.getRecipient())) {
                                        RegistryEntry entry = RegistryService.getInstance().getRegistry().get(chatMessage.getRecipient());

                                        // If not found (means null and non instanceof, it means we're not
                                        // the master node.
                                        if (entry instanceof RoomBaseEntry) {
                                            for (String member : ((RoomBaseEntry) entry).getMembers()) {
                                                if (!RegistryService.getInstance().getRegistry().isSelf(member) && !member.equals(chatMessage.getSender())) {
                                                    RegistryEntry e = RegistryService.getInstance().getRegistry().get(member);
                                                    NetService.getInstance().send(e.getAddress(), msg.group(0));
                                                }
                                            }
                                            // Make sure we have an entry for the chat room if not we create it.
                                        } else if (entry == null) {
                                            entry = new BaseEntry();
                                            ((BaseEntry) entry).setKey(chatMessage.getRecipient());
                                            ((BaseEntry) entry).setNetAddress(udpMessage.getIp());
                                            ((BaseEntry) entry).setPort(udpMessage.getPort());

                                            RegistryService.getInstance().getRegistry().save(entry);
                                        }

                                        // Detach, this will be run the FX Application thread otherwise we can't modify the UI.
                                        Platform.runLater(() -> ChatScene.receiveMessage(chatMessage.getRecipient(), chatMessage.getSender(), chatMessage.getBody()));

                                        // Simple one to one message.
                                    } else {
                                        RegistryEntry entry = RegistryService.getInstance().getRegistry().get(chatMessage.getSender());

                                        // Update the information of the sender.
                                        if (entry instanceof BaseEntry) {
                                            ((BaseEntry) entry).setPort(udpMessage.getPort());
                                            ((BaseEntry) entry).setNetAddress(udpMessage.getIp());

                                            // Create a new entry for the sender.
                                        } else if (entry == null) {
                                            entry = new BaseEntry();
                                            ((BaseEntry) entry).setKey(chatMessage.getSender());
                                            ((BaseEntry) entry).setPort(udpMessage.getPort());
                                            ((BaseEntry) entry).setNetAddress(udpMessage.getIp());
                                        }
                                        RegistryService.getInstance().getRegistry().save(entry);

                                        // Detach, this will be run the FX Application thread otherwise we can't modify the UI.
                                        Platform.runLater(() -> ChatScene.receiveMessage(chatMessage.getSender(), chatMessage.getSender(), chatMessage.getBody()));
                                    }
                                    break;



                                // Manage ACK?

                                // Manage registry refresh.

                                default:
                                    // No default.
                            }
                        }

                    } catch (Exception e) {

                    }
                }
            });
        }

        // USER:ADD:{CLIENT}


        // USER:GET_BY_USERNAME:{json de list de string qui sont les username} (refresh registry).
        // Returns List of Client (as json).

        static {
            MSG_PATTERN = Pattern.compile("(\\w+):(.+)");
        }
    }
}
