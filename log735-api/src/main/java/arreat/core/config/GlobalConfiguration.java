package arreat.core.config;

public class GlobalConfiguration {

    private NetConfiguration netConfiguration;
    private RegistryConfiguration registryConfiguration;

    public NetConfiguration getNetConfiguration() {
        return netConfiguration;
    }

    public void setNetConfiguration(NetConfiguration netConfiguration) {
        this.netConfiguration = netConfiguration;
    }

    public RegistryConfiguration getRegistryConfiguration() {
        return registryConfiguration;
    }

    public void setRegistryConfiguration(RegistryConfiguration registryConfiguration) {
        this.registryConfiguration = registryConfiguration;
    }
}
