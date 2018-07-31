package arreat.api.registry;

import java.net.SocketAddress;

public interface RegistryEntry {
    String getKey();
    String getDisplayName();
    SocketAddress getAddress();
}