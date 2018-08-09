package arreat.api.registry;

import com.google.gson.Gson;
import java.net.SocketAddress;

public interface Entry {

  String getName();

  String getDisplayName();

  SocketAddress getAddress();

  void setName(String name);

  void setDisplayName(String name);

  void setAddress(SocketAddress address);

  default String toJson() {
    return new Gson().toJson(this);
  }
}