package arreat.impl.registry;

import arreat.api.registry.Registry;
import arreat.api.registry.RegistryEntry;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CacheRegistry implements Registry {

    private final Map<String, RegistryEntry> entries;
    private RegistryEntry defaultRemote;
    private RegistryEntry self;
    private final List<RegistryEntry> remotes;

    @SuppressWarnings("unchecked")
    public CacheRegistry(List<? extends RegistryEntry> remotes) {
        this.remotes = (List<RegistryEntry>) remotes;
        this.entries = new HashMap<>();
        this.defaultRemote = this.remotes.get(0);
    }

    @Override
    public boolean contains(String key) {
        return this.entries.containsKey(key);
    }

    @Override
    public RegistryEntry get(String key) {
        return this.entries.get(key);
    }

    @Override
    public boolean isSelf(String key) {
        return this.self.getKey().equals(key);
    }

    @Override
    public boolean isSelf(RegistryEntry entry) {
        return false;
    }

    @Override
    public boolean equals(RegistryEntry entry1, RegistryEntry entry2) {
        return false;
    }

    @Override
    public void save(RegistryEntry entry) {
        this.entries.put(entry.getKey(), entry);
    }

    @Override
    public List<RegistryEntry> getRemotes() {
        return this.remotes;
    }

    @Override
    public RegistryEntry getDefaultRemote() {
        return this.defaultRemote;
    }

    @Override
    public RegistryEntry getSelf() {
        return this.self;
    }

    @Override
    public void setSelf(RegistryEntry self) {
        this.self = self;
    }

    @Override
    public void setDefaultRemote(RegistryEntry defaultRemote) {
        this.defaultRemote = defaultRemote;
    }
}
