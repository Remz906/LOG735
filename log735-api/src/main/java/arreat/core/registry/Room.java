package arreat.core.registry;

import arreat.api.registry.RoomEntry;
import arreat.api.registry.UserEntry;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "rooms", schema = "arreat")
public class Room extends AbstractEntry implements RoomEntry {

  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
  @JoinTable(
      name="room_members",
      joinColumns = @JoinColumn( name="user_id"),
      inverseJoinColumns = @JoinColumn( name="room_id")
  )
  private Set<User> members;

  @OneToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "owner_id")
  private User owner;

  @Override
  public void add(UserEntry member) {
    this.members.add((User) member);
  }

  public Room() {
        this.members = new HashSet<>();
    }

  public Set<UserEntry> getMembers() {
        return this.members.stream().map(UserEntry.class::cast).collect(Collectors.toSet());
    }

  @Override
  public UserEntry getOwner() {
    return this.owner;
  }

  @Override
  public void setOwner(UserEntry owner) {
    this.owner = (User) owner;
  }
}
