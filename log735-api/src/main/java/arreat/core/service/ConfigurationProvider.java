package arreat.core.service;

import arreat.core.config.GlobalConfiguration;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;

public final class ConfigurationProvider {

    private static volatile ConfigurationProvider instance;

    private GlobalConfiguration config;

    private ConfigurationProvider() {
        try (InputStream in = this.getClass().getClassLoader().getResourceAsStream("config.yml")) {
            Yaml yaml = new Yaml();

            this.config = yaml.loadAs(in, GlobalConfiguration.class);

        } catch (IOException e) {
            throw new RuntimeException("Error could not load configuration", e);
        }
    }

    public static synchronized GlobalConfiguration getGlobalConfig() {
        return instance.config;
    }

    static {
        synchronized (ConfigurationProvider.class) {
            ConfigurationProvider provider = instance;

            if (provider == null) {
                instance = new ConfigurationProvider();
            }
        }
    }
}