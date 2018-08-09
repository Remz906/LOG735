package arreat.core.registry;

import arreat.api.registry.OriginEntry;
import java.util.List;

public class H2Registry extends HibernateRegistry {

  public H2Registry(List<? extends OriginEntry> origins) {
    super(origins, "HibernateH2Unit");
  }
}
