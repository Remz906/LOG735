package arreat.core.registry;

import arreat.api.registry.Entry;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import org.hibernate.annotations.GenericGenerator;

@MappedSuperclass
public abstract class AbstractEntry implements Entry {

  @Id @GeneratedValue(generator="system-uuid")
  @GenericGenerator(name="system-uuid",
      strategy = "uuid")
  private String id;

  @Column(nullable = false, unique = true)
  private String name;

  @Column(name = "display_name")
  private String displayName;

  @Column
  private String ip;

  @Column
  private String password;

  @Column
  private int port;

  @Override
  public String getName() {
    return this.name;
  }

  @Override
  public String getDisplayName() {
    return this.displayName;
  }

  @Override
  public SocketAddress getAddress() {
    return new InetSocketAddress(this.ip, this.port);
  }

  @Override
  public void setAddress(SocketAddress address) {
    if (address instanceof InetSocketAddress) {
      this.ip = ((InetSocketAddress) address).getHostName();
      this.port = ((InetSocketAddress) address).getPort();
    }
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  public void setIp(String ip) {
    this.ip = ip;
  }

  public void setPort(int port) {
    this.port = port;
  }

  public String getId() {
    return id;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }
}
