package arreat.core.service;

import arreat.api.registry.Entry;
import arreat.api.registry.OriginEntry;
import arreat.api.registry.RoomEntry;
import arreat.api.registry.UserEntry;
import arreat.api.service.Service;
import arreat.api.registry.Registry;
import arreat.core.registry.CacheRegistry;
import arreat.core.config.RegistryConfiguration;
import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.h2.engine.User;

public final class RegistryService implements Service {

  private static volatile RegistryService instance;

  private Registry registry;

  private RegistryService() {
  }

  public static Set<OriginEntry> listOrigins() {
    return getInstance().getRegistry().getOrigins();
  }

  public static boolean isSelf(Entry entry) {
    return getInstance().getRegistry().isSelf(entry.getName());
  }

  public static OriginEntry originFromJson(String json) {
    return getInstance().getRegistry().originFromJson(json);
  }

  public static void setSelf(Entry self) {
    getInstance().getRegistry().setSelf(self);
  }

  public static UserEntry createUser() {
    return getInstance().getRegistry().createUser();
  }

  public Registry getRegistry() {
    return this.registry;
  }

  public static UserEntry getUserByName(String name) {
    return getInstance().getRegistry().getUserByName(name);
  }

  public static RoomEntry getRoomByName(String name) {
    return getInstance().getRegistry().getRoomByName(name);
  }

  public static List<UserEntry> listUsers() {
    return getInstance().getRegistry().listUsers();
  }

  public static List<RoomEntry> listRooms() {
    return getInstance().getRegistry().listRooms();
  }

  public static UserEntry getUserByAddress(String ip, int port) {
    return getInstance().getRegistry().getUserByAddress(ip, port);
  }

  public static UserEntry userFromJson(String json) {
    return getInstance().getRegistry().userFromJson(json);
  }

  public static RoomEntry roomFromJson(String json) {
    return getInstance().getRegistry().roomFromJson(json);
  }

  public static List<? extends RoomEntry> roomListFromJson(String json) {
    return getInstance().getRegistry().roomListFromJson(json);
  }

  public static List<? extends UserEntry> userListFromJson(String json) {
    return getInstance().getRegistry().userListFromJson(json);
  }

  public static synchronized void close() {
    getInstance().getRegistry().close();
  }

  public static void save(Entry entry) {
    getInstance().getRegistry().save(entry);
  }

  public static void saveAll(Collection<? extends Entry> entries) {
    entries.forEach(e -> getInstance().getRegistry().save(e));
  }

  public static void delete(Entry entry) {
    getInstance().getRegistry().delete(entry);
  }

  public static synchronized RegistryService getInstance() {
    return instance;
  }

  public static Entry getSelf() {
    return getInstance().getRegistry().getSelf();
  }

  public static OriginEntry getMasterOrigin() {
    return getInstance().getRegistry().getMasterOrigin();
  }

  public static void setMasterOrigin(OriginEntry master) {
    getInstance().getRegistry().setMasterOrigin(master);
  }

  public static RoomEntry createRoom() {
    return getInstance().getRegistry().createRoom();
  }

  public static OriginEntry createOrigin() {
    return getInstance().getRegistry().createOrigin();
  }

  @Override
  @SuppressWarnings("unchecked")
  public void configure() {
    RegistryConfiguration cfg = ConfigurationProvider.getGlobalConfig().getRegistryConfiguration();

    // Create the registry base on it's type.
    // Config may contain additional info base on the type of configuration.
    String type = cfg.getType();

    try {
      Class<? extends Registry> registryType = (Class<? extends Registry>) Class.forName(type);
      Constructor ctor = registryType.getDeclaredConstructor(List.class);

      this.registry = (Registry) ctor.newInstance(cfg.getRemotes());

    } catch (Exception err) {
      throw new RuntimeException("Failed to configure registry", err);
    }
  }

  static {
    synchronized (RegistryService.class) {
      RegistryService service = instance;

      if (service == null) {
        instance = new RegistryService();
      }
    }
  }

  @Override
  public EventManager getEventManager() {
    return null;
  }
}
