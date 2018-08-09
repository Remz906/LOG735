package arreat.core.registry;

import arreat.api.registry.OriginEntry;
import java.util.List;

public class MysqlRegistry extends HibernateRegistry {

  public MysqlRegistry(List<? extends OriginEntry> origins) {
    super(origins,"HibernateMysqlUnit");
  }
}
