package arreat.core.registry;

import arreat.api.registry.RegistryEntry;

import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;

public class RoomBaseEntry implements RegistryEntry {

    private String key;
    private String displayName;

    private List<String> members;

    public RoomBaseEntry() {
        this.members = new ArrayList<>();
    }

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
        return null;
    }

    public List<String> getMembers() {
        return this.members;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
}
