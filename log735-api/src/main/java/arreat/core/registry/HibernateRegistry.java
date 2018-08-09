package arreat.core.registry;

import arreat.api.registry.Entry;
import arreat.api.registry.OriginEntry;
import arreat.api.registry.RoomEntry;
import arreat.api.registry.UserEntry;
import java.util.List;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.Persistence;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

public abstract class HibernateRegistry extends AbstractRegistry {

  private final EntityManagerFactory factory;
  private final EntityManager manager;

  public HibernateRegistry(List<? extends OriginEntry> origins, String provider) {
    super(origins);
    this.factory = Persistence.createEntityManagerFactory(provider);
    this.manager = this.factory.createEntityManager();
  }

  public User getUserByName(String name) {
    CriteriaBuilder builder = this.manager.getCriteriaBuilder();

    CriteriaQuery<User> query = builder.createQuery(User.class);
    Root<User> root = query.from(User.class);

    query
        .select(root)
        .where(builder.equal(root.get("name"), name));

    User user = null;

    try {
      user = manager.createQuery(query).getSingleResult();

    } catch (NoResultException ok) {
      // Simply return null.
    }

    return user;
  }

  public User getUserByAddress(String ip, int port) {
    CriteriaBuilder builder = this.manager.getCriteriaBuilder();

    CriteriaQuery<User> query = builder.createQuery(User.class);
    Root<User> root = query.from(User.class);

    query
        .select(root)
        .where(builder.equal(root.get("ip"), ip), builder.equal(root.get("port"), port));

    User user = null;

    try {
      user = manager.createQuery(query).getSingleResult();

    } catch (NoResultException ok) {
      // Simply return null.
    }

    return user;
  }

  public Room getRoomByName(String name) {
    CriteriaBuilder builder = this.manager.getCriteriaBuilder();

    CriteriaQuery<Room> query = builder.createQuery(Room.class);
    Root<Room> root = query.from(Room.class);

    query
        .select(root)
        .where(builder.equal(root.get("name"), name));

    Room room = null;

    try {
      room = manager.createQuery(query).getSingleResult();

    } catch (NoResultException ok) {
      // Simply return null.
    }

    return room;
  }

  @Override
  public void save(Entry entry) {
    this.manager.getTransaction().begin();
    this.manager.persist(entry);
    this.manager.getTransaction().commit();
  }

  @Override
  public void close() {
    this.manager.close();
    this.factory.close();
  }

  @Override
  public List<RoomEntry> listRooms() {
    CriteriaBuilder builder = this.manager.getCriteriaBuilder();

    CriteriaQuery<Room> query = builder.createQuery(Room.class);
    Root<Room> root = query.from(Room.class);

    query.select(root);

    return manager.createQuery(query).getResultList()
        .stream()
        .map(RoomEntry.class::cast)
        .collect(Collectors.toList());
  }

  @Override
  public List<UserEntry> listUsers() {
    CriteriaBuilder builder = this.manager.getCriteriaBuilder();

    CriteriaQuery<User> query = builder.createQuery(User.class);
    Root<User> root = query.from(User.class);

    query.select(root);

    return manager.createQuery(query).getResultList()
        .stream()
        .map(UserEntry.class::cast)
        .collect(Collectors.toList());
  }

  @Override
  public void delete(Entry entry) {
    this.manager.getTransaction().begin();
    this.manager.remove(entry);
    this.manager.getTransaction().commit();
  }
}
