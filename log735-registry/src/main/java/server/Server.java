package server;

import arreat.api.registry.Entry;
import arreat.api.registry.OriginEntry;
import arreat.api.registry.RoomEntry;
import arreat.api.registry.UserEntry;
import arreat.core.service.ConfigurationProvider;
import arreat.core.service.RegistryService;
import arreat.core.service.NetService;
import arreat.core.net.UDPMessage;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Server implements Runnable {

  private static final Pattern MSG_PATTERN;

  private static final int HB_TIMER = 5000;
  private static final String HEARTBEAT_HEADER = "HB";
  private static final String HB_OK = "OK";
  private static final String HB_DOWN = "DOWN";
  private static final String HB_UNSTABLE = "UNSTABLE";

  private static final int MASTER_ELECTION_TIMER = 10000;
  private static final String MASTER_ELECTION = "ME";
  private static final String MASTER_FIND_MASTER = "FIND_MASTER";
  private static final String INFO_IS_MASTER = "isMaster";
  private static final String INFO_FM_RESPONSE = "FMR";
  private long beginTimer;
  private long electionTimer;
  private long lastBDUpdate; //needed for restore

  private static final String USER_HEADER = "USER";
  private static final String USER_GET_BY_USER = "GET_BY_USER";
  private static final String USER_GET_RESPONSE = "GR";

  private static final String ROOM_HEADER = "NODE";
  private static final String NODE_GET_BY_NAME = "GET_BY_NAME";

  private static final String COMMAND_UPDATE = "UPDATE";
  private static final String COMMAND_ADD = "ADD";
  private static final String COMMAND_GOSSIP = "GOSSIP";
  private static final String COMMAND_DELETE = "DELETE";
  private static final String COMMAND_UPDATE_ALL = "UPDATE_ALL";
  private static final String COMMAND_GET_BY_USERNAMES = "GET_BY_USERNAMES";


  private static final String UPDATE_MASTER = "UM";
  private static final String AUTH = "AUTH";

  private boolean shutdownRequested = false;
  private boolean isMaster;
  private Thread hbThread;

  private final Gson gson;
  private boolean broadcast;

  public Server() {
    this.gson = new Gson();
    this.init();
  }

  private void init() {
    NetService.getInstance().configure();
    RegistryService.getInstance().configure();
    this.lastBDUpdate = 0;

    int port = ConfigurationProvider.getGlobalConfig().getNetConfiguration().getReceivingPort();
    String ip = ConfigurationProvider.getGlobalConfig().getNetConfiguration().getReceivingIp();

    InetSocketAddress address = new InetSocketAddress(ip, port);

    for (OriginEntry origin : RegistryService.listOrigins()) {
      if (origin.getAddress().equals(address)) {
        RegistryService.setSelf(origin);
      }
    }

    this.electMaster();
    this.initHeartBeatThread();
  }


  private void electMaster() {
    System.out.println("Starting master election");
    this.isMaster = false;

    OriginEntry master = RegistryService.createOrigin();
    master.setAddress(new InetSocketAddress("127.0.0.1", 0));

    if (electionTimer == 0) {
      electionTimer = System.currentTimeMillis();
    }
    sendCommandToAllServers(
        String.format("%s:%s:%s", MASTER_ELECTION, MASTER_FIND_MASTER,
            String.valueOf(this.lastBDUpdate)));
  }

  private void setMaster(OriginEntry master) {
    this.electionTimer = 0;
    this.beginTimer = System.currentTimeMillis();

    RegistryService.setMasterOrigin(master);

    System.out.println(String.format("New master elected %s", master.toJson()));

    this.isMaster = RegistryService.isSelf(RegistryService.getMasterOrigin());

    if (this.isMaster) {
      sendCommandToAllClients(
          String.format("%s:%s:%s", USER_HEADER, UPDATE_MASTER, new Gson().toJson(master)));

      sendCommandToAllServers(
          String.format("%s:%s:%s", MASTER_ELECTION, UPDATE_MASTER, String.valueOf(lastBDUpdate)));
    }
  }

  private void shutdown() {
    RegistryService.close();
  }

  private void sendCommandToAllServers(String command) {
    for (OriginEntry origin : RegistryService.listOrigins()) {
      if (!RegistryService.isSelf(origin)) {
        NetService.getInstance().send(origin.getAddress(), command);
      }
    }
  }

  private void sendCommandToAllClients(String command) {
    for (UserEntry user : RegistryService.listUsers()) {
      NetService.getInstance().send(user.getAddress(), command);
    }
  }

  public void requestShutdown() {
    this.shutdownRequested = true;
  }

  private void initHeartBeatThread() {
    this.hbThread = new Thread(() -> {
      while (!shutdownRequested) {
        try {
          Thread.sleep(HB_TIMER);

          OriginEntry master = RegistryService.getMasterOrigin();

          if (master != null && !this.isMaster) {
            NetService.getInstance().send(master.getAddress(),
                String.format("%s:%s", HEARTBEAT_HEADER, HB_OK));

          } else if (this.isMaster) {
            beginTimer = System.currentTimeMillis();
          }
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    });
  }

  private void sendACK(String ipAdd, int portNb, String heather) throws UnknownHostException {
    NetService.getInstance().send(ipAdd, portNb, heather + ":" + "ACK");
  }

  private void sendACK(SocketAddress target, String header) {
    NetService.getInstance().send(target, String.format("%s:ACK", header));
  }

  private boolean authenticateUser(String name, String password) {
    UserEntry user = RegistryService.getUserByName(name);
    return user != null && user.getPassword().equals(password);
  }

  private boolean authenticateRoom(String name, String password) {
    RoomEntry room = RegistryService.getRoomByName(name);
    return room != null && room.getPassword().equals(password);
  }

  private void updateDBIfNeeded(SocketAddress target, long otherDbTime) {

    long threshold = 1000;
    if (Math.abs(this.lastBDUpdate - otherDbTime) > threshold && lastBDUpdate > otherDbTime) {
      NetService.getInstance().send(target, String
          .format("%s:%s:%s", USER_HEADER, COMMAND_UPDATE_ALL,
              gson.toJson(RegistryService.listUsers())));

      NetService.getInstance().send(target, String
          .format("%s:%s:%s", ROOM_HEADER, COMMAND_UPDATE_ALL,
              gson.toJson(RegistryService.listRooms())));
    }
  }


  @Override
  public void run() {

    broadcast = false;
    try {
      this.hbThread.start();
      while (!this.shutdownRequested) {
        UDPMessage udpMessage = NetService.getInstance().receive();

        if (udpMessage != null) {
          InetSocketAddress target = new InetSocketAddress(udpMessage.getIp(), udpMessage.getPort());
          Matcher matcher = MSG_PATTERN.matcher(udpMessage.getMsg());

          if (matcher.matches()) {
            switch (matcher.group(1)) {

              case HEARTBEAT_HEADER:
                this.manageHeartbeat(matcher.group(2), target);
                break;

              case MASTER_ELECTION:
                this.manageMasterElection(matcher.group(2), target);
                break;

              case USER_HEADER:
                this.manageUsers(matcher.group(2), target);
                break;

              case ROOM_HEADER:
                this.manageRooms(matcher.group(2), target);
                break;
            }
          }

          //propagate throughout the network
          if (isMaster && broadcast) {
            System.out.println("SYNCHRONIZING --> " + matcher);
            sendCommandToAllServers(udpMessage.getMsg());
          }
        } else {
          Thread.sleep(1000);
        }

        long curTime = System.currentTimeMillis();
        if (electionTimer != 0 && Math.abs(curTime - electionTimer) >= 3000) {
          setMaster((OriginEntry) RegistryService.getSelf());
        }

        if (beginTimer != 0 && Math.abs(curTime - beginTimer) >= MASTER_ELECTION_TIMER) {
          System.out.println("master connection lost");
          electMaster();
        }

        if (udpMessage != null) {
          InetSocketAddress target = new InetSocketAddress(udpMessage.getIp(), udpMessage.getPort());

          if (this.isGreaterThanMaster(target)) {
            for (OriginEntry origin : RegistryService.listOrigins()) {
              if (origin.getAddress().equals(target)) {
                this.setMaster(origin);
                break;
              }
            }
          }
        }
      }
    } catch (Exception e) {
      e.printStackTrace();

    } finally {
      this.shutdown();
    }
  }

  private int compareAddresses(InetSocketAddress address1, InetSocketAddress address2) {
    BigInteger val1 = new BigInteger(address1.getHostName().getBytes());
    val1 = val1.add(BigInteger.valueOf(address1.getPort()));

    BigInteger val2 =
        new BigInteger(address2.getHostName().getBytes());

    val2 =
        val2.add(BigInteger.valueOf(address2.getPort()));

    return val1.compareTo(val2);
  }

  private boolean isLesserThanMaster(InetSocketAddress address) {
    OriginEntry master = RegistryService.getMasterOrigin();
    return compareAddresses(address, (InetSocketAddress) master.getAddress()) < 0;
  }


  private boolean isGreaterThanSelf(InetSocketAddress address) {
    Entry self = RegistryService.getSelf();
    return compareAddresses(address, (InetSocketAddress) self.getAddress()) > 0;
  }

  private boolean isLesserThanSelf(InetSocketAddress address) {
    Entry self = RegistryService.getSelf();
    return compareAddresses(address, (InetSocketAddress) self.getAddress()) < 0;
  }

  private void manageRooms(String message, SocketAddress target) {
    Matcher matcher = MSG_PATTERN.matcher(message);

    if (matcher.matches()) {

      switch (matcher.group(1)) {
        case COMMAND_ADD:
        case COMMAND_UPDATE:
          this.manageRoomEntry(matcher.group(2), target);
          break;

        case COMMAND_UPDATE_ALL:
          this.manageRoomEntries(matcher.group(2), target);
          break;

        case COMMAND_DELETE:
          this.manageRoomDelete(matcher.group(2), target);

          break;
        case NODE_GET_BY_NAME:
          this.manageRoomRequest(matcher.group(2), target);
          break;

        case AUTH:
          String[] values = matcher.group(2).split(":");
          this.manageRoomAuthentication(values[0], values[1], target);
          break;
      }
    }
    this.lastBDUpdate = System.currentTimeMillis();
  }

  private void manageRoomAuthentication(String name, String password, SocketAddress target) {
    RoomEntry room = RegistryService.getRoomByName(name);

    // Get new member
    UserEntry member = RegistryService
        .getUserByAddress(((InetSocketAddress) target).getHostName(),
            ((InetSocketAddress) target).getPort());

    if (room != null && this.authenticateRoom(name, password)) {

      UserEntry owner = room.getOwner();

      room.add(member);
      RegistryService.save(room);

      NetService.getInstance().send(owner.getAddress(),
          String.format("%s:%s:%s", ROOM_HEADER, room.getName(), member.toJson()));

      // Workaround to set the right name for the conversation.
      String json = owner.toJson().replaceAll("\"name\":\"(\\w+)\"",
          String.format("\"name\":\"%s\"", room.getName()));

      NetService.getInstance().send(target,
          String.format("%s:%s:%s", ROOM_HEADER, AUTH, json));

    } else if (room == null) {
      room = RegistryService.createRoom();
      room.setOwner(member);
      room.setName(name);
      room.setPassword(password);

      RegistryService.save(room);

      NetService.getInstance().send(target, String.format("%s:CREATED:%s", ROOM_HEADER, name));
      sendCommandToAllServers(String.format("%s:%s:%s", ROOM_HEADER, COMMAND_ADD, room.toJson()));

    } else {
      NetService.getInstance().send(target, String.format("%s:%s:Failed", USER_HEADER, AUTH));
    }
  }

  private void manageRoomRequest(String name, SocketAddress target) {
    NetService.getInstance().send(target,
        String.format("%s:%s:%s", USER_HEADER, USER_GET_RESPONSE,
            RegistryService.getRoomByName(name).toJson()));
  }

  private void manageRoomDelete(String json, SocketAddress target) {
    RoomEntry room = RegistryService.roomFromJson(json);
    RegistryService.save(room);

    this.sendACK(target, ROOM_HEADER);

    this.broadcast = true;
  }

  private void manageRoomEntries(String json, SocketAddress target) {
    List<? extends RoomEntry> rooms = RegistryService.roomListFromJson(json);
    RegistryService.saveAll(rooms);

    this.sendACK(target, ROOM_HEADER);

    this.broadcast = true;
  }

  private void manageRoomEntry(String json, SocketAddress target) {
    RoomEntry room = RegistryService.roomFromJson(json);
    RegistryService.save(room);

    this.sendACK(target, ROOM_HEADER);

    this.broadcast = true;
  }

  private void manageUsers(String message, SocketAddress target) throws UnknownHostException {
    Matcher matcher = MSG_PATTERN.matcher(message);

    if (matcher.matches()) {
      switch (matcher.group(1)) {
        case COMMAND_ADD:
          this.manageUserCreate(matcher.group(2), target);
          break;

        case COMMAND_UPDATE:
          this.manageUserUpdate(matcher.group(2), target);
          break;

        case COMMAND_UPDATE_ALL:
          this.manageUsersUpdate(matcher.group(2), target);
          break;

        case COMMAND_DELETE:
          this.manageUserDelete(matcher.group(2), target);
          break;

        case USER_GET_BY_USER:
          this.manageUserRequest(matcher.group(2), target);
          break;

        case COMMAND_GET_BY_USERNAMES:
          this.manageUserListRequest(matcher.group(2), target);
          break;

        case AUTH:
          String[] values = matcher.group(2).split(":");

          this.manageUserAuthentication(values[0], values[1], target);
          break;
      }
    }
  }

  private void manageUserAuthentication(String name, String password, SocketAddress target) {

    if (this.authenticateUser(name, password)) {
      UserEntry user = RegistryService.getUserByName(name);

      // Update the address of the user if needed.
      if (!user.getAddress().equals(target)) {
        user.setAddress(target);

        RegistryService.save(user);

        // Don't use the default toJson, cause it doesn't returns passwords.
        this.sendCommandToAllServers(
            String.format("%s:%s:%s", USER_HEADER, COMMAND_UPDATE, this.gson.toJson(user)));

        this.broadcast = false;
      }

      NetService.getInstance().send(target,
          String.format("%s:%s:%s", USER_HEADER, AUTH, user.toJson()));

    } else {
      NetService.getInstance().send(target, String.format("%s:%s:Failed", USER_HEADER, AUTH));
    }
  }

  private void manageUserRequest(String name, SocketAddress target) {
    UserEntry user = RegistryService.getUserByName(name);

    NetService.getInstance().send(target,
        String.format("%s:%s:%s", USER_HEADER, USER_GET_RESPONSE, user.toJson()));
  }

  private void manageUserDelete(String json, SocketAddress target) {
    UserEntry user = RegistryService.userFromJson(json);
    RegistryService.delete(user);

    this.lastBDUpdate = System.currentTimeMillis();
    this.sendACK(target, USER_HEADER);

    this.broadcast = true;
  }

  private void manageUsersUpdate(String json, SocketAddress target) {
    List<? extends UserEntry> users = RegistryService.userListFromJson(json);
    RegistryService.saveAll(users);

    this.lastBDUpdate = System.currentTimeMillis();
    this.sendACK(target, USER_HEADER);

    this.broadcast = true;
  }

  private void manageUserUpdate(String json, SocketAddress target) {
    UserEntry user = RegistryService.userFromJson(json);
    user.setAddress(target);

    RegistryService.save(user);

    this.lastBDUpdate = System.currentTimeMillis();
    this.sendACK(target, USER_HEADER);

    this.broadcast = true;
  }

  private void manageUserCreate(String json, SocketAddress target) {
    UserEntry user = RegistryService.userFromJson(json);
    user.setAddress(target);

    if (RegistryService.getUserByName(user.getName()) == null) {
      RegistryService.save(user);
      this.lastBDUpdate = System.currentTimeMillis();

      NetService.getInstance().send(target,
          String.format("%s:%s:%s", USER_HEADER, AUTH, user.toJson()));

    } else {
      NetService.getInstance().send(target, String.format("%s:%s:Failed", USER_HEADER, AUTH));
    }
    this.broadcast = true;
  }

  private void doMasterElection(SocketAddress target) {
    if (this.isGreaterThanMaster((InetSocketAddress) target)) {
      if (this.isGreaterThanSelf((InetSocketAddress) target)) {
        for (OriginEntry origin : RegistryService.listOrigins()) {
          if (origin.getAddress().equals(target)) {
            this.setMaster(origin);
          }
        }
      } else {
        this.setMaster((OriginEntry) RegistryService.getSelf());
      }
    }
  }

  private void manageMasterElection(String message, SocketAddress target) {
    Matcher matcher = MSG_PATTERN.matcher(message);

    if (matcher.matches()) {
      switch (matcher.group(1)) {
        case MASTER_FIND_MASTER:
        case UPDATE_MASTER:
          this.doMasterElection(target);

          NetService.getInstance().send(target,
              String.format("%s:%s:%s", MASTER_ELECTION, INFO_FM_RESPONSE,
                  String.valueOf(lastBDUpdate)));

          updateDBIfNeeded(target, Long.parseLong(matcher.group(2)));
          break;

        case INFO_FM_RESPONSE:
          this.doMasterElection(target);

          updateDBIfNeeded(target, Long.parseLong(matcher.group(2)));
          break;
      }
    }
  }

  private void manageHeartbeat(String message, SocketAddress target) {
    if (HB_OK.equals(message)) {
      this.sendACK(target, HEARTBEAT_HEADER);
      this.beginTimer = System.currentTimeMillis();

    } else if ("ACK".equals(message)) {
      this.beginTimer = System.currentTimeMillis();
    }
  }

  private void manageUserListRequest(String json, SocketAddress requester) {
    List<String> names = this.gson.fromJson(json, new TypeToken<List<String>>() {
    }.getType());
    List<UserEntry> users = new ArrayList<>();

    for (String name : names) {
      UserEntry u = RegistryService.getUserByName(name);

      if (u != null) {
        users.add(u);
      }
    }
    NetService.getInstance().send(requester,
        String.format("%s:%s:%s", USER_HEADER, COMMAND_GET_BY_USERNAMES, gson.toJson(users)));
  }

  private boolean isGreaterThanMaster(InetSocketAddress address) {
    OriginEntry master = RegistryService.getMasterOrigin();
    return compareAddresses(address, (InetSocketAddress) master.getAddress()) > 0;
  }

  static {
    MSG_PATTERN = Pattern.compile("(\\w+):(.+)");
  }
}
