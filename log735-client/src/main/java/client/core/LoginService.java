package client.core;

import arreat.api.registry.RegistryEntry;
import arreat.impl.core.RegistryService;
import arreat.api.registry.Registry;
import arreat.impl.core.NetService;
import arreat.api.core.Service;
import client.Client;
import client.event.LoginEvent;
import client.ui.ChatScene;
import com.google.common.eventbus.Subscribe;

public final class LoginService implements Service {
    private static volatile LoginService instance;

    public static synchronized LoginService getInstance() {
        return instance;
    }

    public void login(String username, String password) {
        Registry registry = RegistryService.getInstance().getRegistry();

        RegistryEntry defaultRemote = registry.getDefaultRemote();

        NetService.getInstance().send(defaultRemote.getAddress(), String.format("UF:AUTH:%s:%s", username, password));
    }

    @Override
    public void configure() {

    }

    @Override
    public EventManager getEventManager() {
        return null;
    }

    public void register(String username, String password) {

    }

    private static class LoginEventManager implements EventManager {

        private LoginEventManager() {

        }

        @Subscribe
        public synchronized void handleLoginEvent(LoginEvent event) {
            if (event.isSuccess()) {
                Client.switchScene(new ChatScene(), "Chat");
            }
        }
    }

    static {
        synchronized (LoginService.class) {
            LoginService service = instance;

            if (instance == null) {
                instance = new LoginService();
            }
        }
    }
}
