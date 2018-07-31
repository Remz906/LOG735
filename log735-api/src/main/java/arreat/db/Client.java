package arreat.db;

import java.io.Serializable;
import java.util.Objects;

public class Client implements Serializable {
    private int id;
    private String ip;
    private int port;
    private String pseudo;
    private String pwd;

    public Client(int id, String ip, int port, String pseudo, String pwd) {
        this.id = id;
        this.ip = ip;
        this.port = port;
        this.pseudo = pseudo;
        this.pwd = pwd;
    }

    public int getId(){return id;}
    public void setId(int id){this.id = id;}
    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    public String getPseudo() {
        return pseudo;
    }

    public String getPwd(){return pwd;}

    public void setPwd(String pwd){this.pwd = pwd;}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Client client = (Client) o;
        return port == client.port &&
                Objects.equals(ip, client.ip) &&
                Objects.equals(pseudo, client.pseudo) &&
                Objects.equals(id, client.id);
    }
}
