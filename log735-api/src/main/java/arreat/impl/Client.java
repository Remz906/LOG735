package arreat.impl;

import java.util.Objects;

public class Client{
    private String ip;
    private int port;
    private String pseudo;

    public Client(String ip, int port, String pseudo) {
        this.ip = ip;
        this.port = port;
        this.pseudo = pseudo;
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    public String getPseudo() {
        return pseudo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Client client = (Client) o;
        return port == client.port &&
                Objects.equals(ip, client.ip) &&
                Objects.equals(pseudo, client.pseudo);
    }
}
