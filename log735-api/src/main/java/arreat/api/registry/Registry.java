package arreat.api.registry;

import java.util.List;

public interface Registry {
    boolean contains(String key);
    RegistryEntry get(String key);
    boolean isSelf(String key);
    boolean isSelf(RegistryEntry entry);
    boolean equals(RegistryEntry entry1, RegistryEntry entry2);
    void save(RegistryEntry entry);
    List<RegistryEntry> getRemotes();
    RegistryEntry getDefaultRemote();
}
