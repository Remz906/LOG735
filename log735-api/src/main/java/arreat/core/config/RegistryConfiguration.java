package arreat.core.config;

import arreat.core.registry.BaseEntry;

import java.util.List;

public class RegistryConfiguration {

    private String type;

    private List<BaseEntry> remotes;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<BaseEntry> getRemotes() {
        return remotes;
    }

    public void setRemotes(List<BaseEntry> remotes) {
        this.remotes = remotes;
    }
}
