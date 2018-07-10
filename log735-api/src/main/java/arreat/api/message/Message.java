package arreat.api.message;

import arreat.api.registry.RegistryEntry;

public interface Message {

    RegistryEntry getTarget();
    Body getBody();
    void setBody(Body body);
    boolean isAcknowledgmentRequired();
}