package client.event;

public class LoginEvent {

    private final boolean success;

    public LoginEvent(boolean success) {
        this.success = success;
    }

    public boolean isSuccess() {
        return this.success;
    }
}
