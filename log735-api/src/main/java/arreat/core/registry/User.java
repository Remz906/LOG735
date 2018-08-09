package arreat.core.registry;

import arreat.api.registry.UserEntry;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "users", schema = "arreat")
public class User extends AbstractEntry implements UserEntry {

  @Override
  public int hashCode() {
    return this.getName().hashCode();
  }
}
