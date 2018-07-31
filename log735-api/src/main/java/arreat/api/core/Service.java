package arreat.api.core;

public interface Service {

    void configure();

    EventManager getEventManager();

    interface EventManager {

    }
}
