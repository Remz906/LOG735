package arreat.core.registry;

import arreat.api.registry.OriginEntry;
import arreat.api.registry.Registry;
import arreat.api.registry.Entry;

import arreat.api.registry.RoomEntry;
import arreat.api.registry.UserEntry;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class CacheRegistry extends AbstractRegistry {

  private final Map<String, UserEntry> users;
  private final Map<String, RoomEntry> rooms;

  @SuppressWarnings("unchecked")
  public CacheRegistry(List<? extends OriginEntry> origins) {
    super(origins);
    this.users = new HashMap<>();
    this.rooms = new HashMap<>();
  }

  @Override
  public UserEntry getUserByName(String name) {
    return this.users.get(name);
  }

  @Override
  public UserEntry getUserByAddress(String ip, int port) {
    UserEntry user = null;
    for (Map.Entry<String, UserEntry> entry : this.users.entrySet()) {
      InetSocketAddress address = (InetSocketAddress) entry.getValue().getAddress();

      if (address.getHostName().equals(ip) && address.getPort() == port) {
        user = entry.getValue();
        break;
      }
    }
    return user;
  }

  @Override
  public void delete(Entry entry) {
    if (entry instanceof UserEntry) {
      this.users.remove(entry.getName());

    } else if (entry instanceof RoomEntry) {
      this.rooms.remove(entry.getName());
    }
  }

  @Override
  public List<UserEntry> listUsers() {
    return this.users.entrySet().stream().map(Map.Entry::getValue).collect(Collectors.toList());
  }

  @Override
  public List<RoomEntry> listRooms() {
    return this.rooms.entrySet().stream().map(Map.Entry::getValue).collect(Collectors.toList());
  }

  @Override
  public RoomEntry getRoomByName(String name) {
    return this.rooms.get(name);
  }

  @Override
  public void save(Entry entry) {
    if (entry instanceof UserEntry) {
      this.users.put(entry.getName(), (UserEntry) entry);
    } else if (entry instanceof RoomEntry) {
      this.rooms.put(entry.getName(), (RoomEntry) entry);

      for (UserEntry member : ((RoomEntry) entry).getMembers()) {
        this.users.put(member.getName(), member);
      }
    }
  }
}
