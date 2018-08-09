package arreat.api.registry;

import java.util.Set;

public interface RoomEntry extends Entry {

  Set<UserEntry> getMembers();

  UserEntry getOwner();

  void setOwner(UserEntry owner);

  void add(UserEntry member);

  String getPassword();

  void setPassword(String password);
}
