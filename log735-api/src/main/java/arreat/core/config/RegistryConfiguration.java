package arreat.core.config;

import arreat.core.registry.Origin;
import arreat.core.registry.User;

import java.util.List;

public class RegistryConfiguration {

  private String type;

  private List<Origin> remotes;

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public List<Origin> getRemotes() {
    return remotes;
  }

  public void setRemotes(List<Origin> remotes) {
    this.remotes = remotes;
  }
}
