package arreat.core.service;

import arreat.api.service.Service;
import arreat.api.registry.Registry;
import arreat.core.registry.CacheRegistry;
import arreat.core.config.RegistryConfiguration;

public final class RegistryService implements Service {

    private static volatile RegistryService instance;

    private Registry registry;

    private RegistryService() {
    }

    public Registry getRegistry() {
        return this.registry;
    }

    public static synchronized RegistryService getInstance() {
        return instance;
    }

    static {
        synchronized (RegistryService.class) {
            RegistryService service = instance;

            if (service == null) {
                instance = new RegistryService();
            }
        }
    }

    @Override
    public void configure() {
        RegistryConfiguration cfg = ConfigurationProvider.getGlobalConfig().getRegistryConfiguration();

        // Create the registry base on it's type.
        // Config may contain additional info base on the type of configuration.
        String type = cfg.getType();

        if (CacheRegistry.class.getName().equals(type)) {
            this.registry = new CacheRegistry(cfg.getRemotes());
        }
    }

    @Override
    public EventManager getEventManager() {
        return null;
    }
}
