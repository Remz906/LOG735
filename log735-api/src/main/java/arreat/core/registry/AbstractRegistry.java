package arreat.core.registry;

import arreat.api.registry.Entry;
import arreat.api.registry.OriginEntry;
import arreat.api.registry.Registry;
import arreat.api.registry.RoomEntry;
import arreat.api.registry.UserEntry;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class AbstractRegistry implements Registry {

  private final Set<OriginEntry> origins;
  private OriginEntry master;
  private Entry self;

  public AbstractRegistry(List<? extends OriginEntry> origins) {
    this.origins = new HashSet<>(origins);
    this.master = origins.get(0);
  }

  public void setMasterOrigin(OriginEntry master) {
    this.master = master;
  }

  public OriginEntry getMasterOrigin() {
    return master;
  }

  @Override
  public Set<OriginEntry> getOrigins() {
    return origins;
  }

  @Override
  public void setSelf(Entry self) {
    this.self = self;
  }

  @Override
  public Entry getSelf() {
    return self;
  }

  @Override
  public boolean isSelf(String name) {
    return this.self != null && this.self.getName().equals(name);
  }

  @Override
  public boolean isSelf(Entry entry) {
    return entry.equals(this.self);
  }

  @Override
  public void close() {

  }

  @Override
  public List<? extends UserEntry> userListFromJson(String json) {
    Gson gson = new Gson();

    return gson.fromJson(json, new TypeToken<List<User>>() {}.getType());
  }

  @Override
  public UserEntry userFromJson(String json) {
    Gson gson = new Gson();

    return gson.fromJson(json, User.class);
  }

  @Override
  public List<? extends RoomEntry> roomListFromJson(String json) {
    Gson gson = new Gson();

    return gson.fromJson(json, new TypeToken<List<Room>>() {}.getType());
  }

  @Override
  public RoomEntry roomFromJson(String json) {
    Gson gson = new Gson();

    return gson.fromJson(json, Room.class);
  }

  @Override
  public RoomEntry createRoom() {
    return new Room();
  }

  public OriginEntry createOrigin() {
    return new Origin();
  }

  @Override
  public OriginEntry originFromJson(String json) {
    Gson gson = new Gson();

    return gson.fromJson(json, Origin.class);
  }

  public UserEntry createUser() {
    return new User();
  }
}
