package arreat.core.registry;

import arreat.api.registry.RegistryEntry;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

public class BaseEntry implements RegistryEntry {

    private String key;
    private String displayName;
    private String netAddress;
    private int port;

    @Override
    public String getKey() {
        return this.key;
    }

    @Override
    public String getDisplayName() {
        return this.displayName;
    }

    @Override
    public SocketAddress getAddress() {
        return new InetSocketAddress(this.netAddress, this.port);
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void setNetAddress(String netAddress) {
        this.netAddress = netAddress;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
