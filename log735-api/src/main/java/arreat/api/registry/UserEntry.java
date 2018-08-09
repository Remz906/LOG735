package arreat.api.registry;

public interface UserEntry extends Entry {

  String getPassword();

  void setPassword(String password);
}
