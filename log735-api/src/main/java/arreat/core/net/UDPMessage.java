package arreat.core.net;

public class UDPMessage {

    String ip;
    int port;
    String msg;


    public UDPMessage(String ip, int port, String msg){
        this.ip = ip.replaceAll ("/","");
        this.port = port;
        this.msg = msg;
    }


    public String getIp() {
        return ip;
    }

    public UDPMessage setIp(String ip) {
        this.ip = ip;
        return this;
    }

    public int getPort() {
        return port;
    }

    public UDPMessage setPort(int port) {
        this.port = port;
        return this;
    }

    public String getMsg() {
        return msg;
    }

    public UDPMessage setMsg(String msg) {
        this.msg = msg;
        return this;
    }
}