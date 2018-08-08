package arreat.api.service;

public interface Service {

    void configure();

    EventManager getEventManager();

    interface EventManager {

    }
}
