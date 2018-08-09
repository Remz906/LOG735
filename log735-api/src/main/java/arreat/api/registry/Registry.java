package arreat.api.registry;

import java.util.List;
import java.util.Set;

public interface Registry {

  OriginEntry createOrigin();

  void delete(Entry entry);

  List<UserEntry> listUsers();

  List<RoomEntry> listRooms();

  UserEntry getUserByName(String name);

  UserEntry getUserByAddress(String ip, int port);

  RoomEntry getRoomByName(String name);

  boolean isSelf(String key);

  boolean isSelf(Entry entry);

  void save(Entry entry);

  Set<OriginEntry> getOrigins();

  OriginEntry getMasterOrigin();

  Entry getSelf();

  void setSelf(Entry self);

  void setMasterOrigin(OriginEntry defaultRemote);

  void close();

  List<? extends UserEntry> userListFromJson(String json);

  List<? extends RoomEntry> roomListFromJson(String json);

  UserEntry userFromJson(String json);

  RoomEntry roomFromJson(String json);

  RoomEntry createRoom();

  OriginEntry originFromJson(String json);

  UserEntry createUser();
}
