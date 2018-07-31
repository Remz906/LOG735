package arreat.api.core;

import arreat.api.message.Message;
import com.google.common.eventbus.EventBus;

import java.util.HashSet;
import java.util.Set;

public final class EventService implements Service {

    private static volatile EventService instance;
    private final Set<String> registeredServices;

    private final EventBus bus;

    private EventService() {
        this.bus = new EventBus();
        this.registeredServices = new HashSet<>();
    }

    public static synchronized EventService getInstance() {
        return instance;
    }

    public void register(Service service) {
        if (!this.registeredServices.contains(service.getClass().getName())) {

            this.registeredServices.add(service.getClass().getName());

            EventManager manager = service.getEventManager();

//            new Thread(manager).start();

            // We're are not registering the service since it's need to be synchronized
            // for safe thread usage, the EventManager does that when it's @Subscribe
            // methods are called.
            this.bus.register(manager);
        }
    }

    public void notify(Message message) {
        this.bus.post(message);
    }

    @Override
    public void configure() {

    }

    @Override
    public EventManager getEventManager() {
        return null;
    }

    static {
        synchronized (EventService.class) {
            EventService service = instance;

            if (service == null) {
                instance = new EventService();
            }
        }
    }
}
