package arreat.api.registry;

public interface Registry {
    RegistryEntry get(String key);
    boolean isSelf(String key);
    boolean isSelf(RegistryEntry entry);
    boolean equals(RegistryEntry entry1, RegistryEntry entry2);
}
