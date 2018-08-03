package arreat.app;

import arreat.api.cfg.GlobalConfiguration;
import arreat.api.cfg.Configuration;
import arreat.api.message.Message;
import arreat.api.pubsub.ServiceBus;
import arreat.api.service.Service;
import arreat.app.service.HeartbeatService;
import arreat.app.service.NetService;
import arreat.app.service.RegistryService;
import com.google.common.util.concurrent.MoreExecutors;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

public final class ArreatApp {

  private final ExecutorService threads;
  private final GlobalConfiguration cfg;
  private final Set<Service> services;
  private final ServiceBus bus;

  public ArreatApp(Service... services) {
    // Load the cfg.
    this.cfg = this.loadConfiguration();

    // Create the service bus.
    this.bus = this.createServiceBus(this.cfg);

    // Load the custom and default services.
    this.services = this.loadAllServices(services);

    // Bootstrap the application.
    this.threads = this.bootstrap();
  }

  private ExecutorService bootstrap() {
    Set<Service> asyncServices = new HashSet<>();

    // Cycles through the services.
    for (Service service : this.services) {

      // Subscribe the service for the message they consumes
      for (Class<? extends Message> type : service.getConsumedMessagesType()) {
        this.bus.subscribe(service, type);
      }

      // Allow the service to publish.
      service.setServiceBus(this.bus);

      // Configure the service
      service.configure(this.cfg.getConfiguration(service.getClass()));

      // Prepare async services to be execute in their own thread.
      if (service.asynchronous()) {
        asyncServices.add(service);
      }
    }

    // Create a pool the size of the async services.
    ExecutorService threads = MoreExecutors.getExitingExecutorService((ThreadPoolExecutor)
        Executors.newFixedThreadPool(asyncServices.size()), 1000, TimeUnit.MILLISECONDS);

    // Submit the element to the pool
    asyncServices.forEach(threads::submit);

    return threads;
  }

  private ServiceBus createServiceBus(GlobalConfiguration cfg) {
    String className = cfg.getConfiguration("ServiceBus").getString("serviceBusImpl");
    ServiceBus bus;

    if (className == null) {
      throw new RuntimeException("Unable to create service bus.");
    }

    try {
      Class<?> clz = Class.forName(className);

      Constructor ctor = clz.getDeclaredConstructor();
      ctor.setAccessible(true);

      bus = (ServiceBus) ctor.newInstance();

    } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException
        | IllegalAccessException | InvocationTargetException err) {
      throw new RuntimeException("Unable to create service bus.", err);
    }
    return bus;
  }

  private Set<Service> loadAllServices(Service... services) {
    Set<Service> allServices = services == null
        ? new HashSet<>() : Arrays.stream(services).collect(Collectors.toSet());

    // Load default services.
    allServices.add(new HeartbeatService());
    allServices.add(new NetService());
    allServices.add(new RegistryService());

    return allServices;
  }

  private GlobalConfiguration loadConfiguration() {
    GlobalConfiguration cfg;

    try (InputStream in = this.getClass().getClassLoader().getResourceAsStream("config.yml")) {
      cfg = new Yaml().loadAs(in, ArreatConfiguration.class);

    } catch (IOException err) {
      throw new RuntimeException("Error could not load configuration", err);
    }
    return cfg;
  }

  private static class ArreatConfiguration implements GlobalConfiguration {

    private Map<String, Configuration> configurations;

    @Override
    public Configuration getConfiguration(String name) {
      Configuration cfg = null;

      if (this.configurations != null) {
        cfg = this.configurations.get(name);
      }
      return cfg;
    }

    public void setConfigurations(Map<String, Configuration> configurations) {
      this.configurations = configurations;
    }
  }
}
