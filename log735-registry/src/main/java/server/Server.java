package server;

import arreat.db.Client;
import arreat.db.DatabaseMySQL;
import arreat.db.Node;
import arreat.impl.core.NetService;
import arreat.impl.net.UDPMessage;
import com.google.gson.Gson;
import com.sun.xml.internal.bind.v2.TODO;

import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.List;

public class Server implements Runnable {

    private static final int HB_TIMER = 10000;
    private static final String HB_MSG_HEATHER = "HB";
    private static final String HB_OK = "OK";
    private static final String HB_DOWN = "DOWN";
    private static final String HB_UNSTABLE = "UNSTABLE";

    private static final int MASTER_ELECTION_TIMER = 500000;
    private static final String MASTER_ELECTION = "ME";
    private static final String MASTER_FIND_MASTER = "FIND_MASTER";
    private static final String INFO_IS_MASTER = "isMaster";
    private static final String INFO_FM_RESPONSE = "FMR";
    private long beginTimer;
    private long lastBDUpdate; //needed for restore

    private static final String USER_HEATHER = "USER";

    private static final String USER_GET_BY_PSEUDO = "GET_BY_PSEUDO";
    private static final String USER_GET_RESPONSE = "GR";

    private static final String COMMAND_UPDATE = "UPDATE";
    private static final String COMMAND_ADD = "ADD";
    private static final String COMMAND_GOSSIP = "GOSSIP";
    private static final String COMMAND_DELETE = "DELETE";


    private static final String USER_FRONT_HEATHER = "UF";
    private static final String UF_UPDATE_MASTER = "UM";
    private static final String UF_AUTH = "AUTH";


    private static final String NODE_HEATHER = "NODE";
    private static final String NODE_GET_BY_NAME = "GET_BY_NAME";


    private boolean shutdownRequested = false;
    private int portNb = 8080;
    private String ipAdd = "127.0.0.0";
    private DatabaseMySQL db;
    private boolean isMaster;
    private String masterIp;
    private int masterPort;
    private Thread hbThread;
    private List<Pair<String, Integer>> ipAddOfServers;

    private boolean serverFound;


    public Server(int portNb, String ipAdd, List<Pair<String, Integer>> ipOfServers) throws SocketException {
        this.ipAdd = ipAdd;
        this.portNb = portNb;
        this.ipAddOfServers = ipOfServers;
        NetService.getInstance().setIpAddress(ipAdd);
        NetService.getInstance().setPortNumber(portNb);
        NetService.getInstance().init();
        init();
    }

    private void init() {
        db = new DatabaseMySQL();
        electMaster();
        initHeartBeatThread();
        lastBDUpdate = System.currentTimeMillis();
    }


    private void electMaster() {
        try {
            masterIp = ipAdd;
            sendCommandToAllServers(MASTER_ELECTION + ":" + MASTER_FIND_MASTER);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    private void setMaster(String ip, int portNb) {
        this.ipAdd = ip;
        this.portNb = portNb;
        boolean preIsMaster = isMaster;
        isMaster = (ipAdd.equals(masterIp));
        if (!preIsMaster && isMaster) {
            try {
                sendCommandToAllClients(USER_FRONT_HEATHER + ":" + UF_UPDATE_MASTER + ":" + ipAdd + ":" + String.valueOf(portNb));
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        }
    }

    private void shutdown() {
        this.db.disconnect();
    }

    private void sendCommandToAllServers(String command) throws UnknownHostException {
        for (Pair<String, Integer> server : ipAddOfServers) {
            NetService.getInstance().send(server.getL(), server.getR(), command);
        }
    }

    private void sendCommandToAllClients(String command) throws UnknownHostException {
        List<Client> users = this.db.getAllClients();
        for (Client clt : users) {
            NetService.getInstance().send(clt.getIp(), clt.getPort(), command);
        }
    }

    public void requestShutdown() {
        this.shutdownRequested = true;
    }

    private void initHeartBeatThread() {
        this.hbThread = new Thread(() -> {
            while (!shutdownRequested) {
                try {
                    Thread.sleep(HB_TIMER);
                    if (masterIp != null && !isMaster) {
                        NetService.getInstance().send(masterIp, masterPort, HB_MSG_HEATHER + ":" + HB_OK);
                    }
                } catch (InterruptedException | UnknownHostException e) {
                    e.printStackTrace();
                }

            }
        });
    }

    public void sendACK(String ipAdd, int portNb, String heather) throws UnknownHostException {
        NetService.getInstance().send(ipAdd, portNb, heather + ":" + "ACK");
    }

    private boolean authClt(String pseudo, String pass) {
        Client cltTrue = this.db.getClientByPseudo(pseudo);
        return cltTrue.getPwd().equals(pass);
    }


    @Override
    public void run() {

        boolean gossip = true;
        try {
            this.hbThread.start();
            while (!this.shutdownRequested) {
                gossip = true;
                UDPMessage udpMessage = NetService.getInstance().receive();
                Gson gson = new Gson();
                String[] msg = udpMessage.getMsg().split(":");
                switch (msg[0]) {
                    case HB_MSG_HEATHER:
                        if (HB_OK.equals(msg[1])) {
                            sendACK(udpMessage.getIp(), udpMessage.getPort(), HB_MSG_HEATHER);
                        } else if ("ACK".equals(msg[1])) {
                            beginTimer = System.currentTimeMillis();
                        }
                        break;
                    case MASTER_ELECTION:
                        switch (msg[1]) {
                            case MASTER_FIND_MASTER:
                                if (Utilities.ipAndPortToLong(udpMessage.getIp(), udpMessage.getPort()) > Utilities.ipAndPortToLong(masterIp, masterPort)) {
                                    setMaster(udpMessage.getIp(), udpMessage.getPort());
                                }
                                NetService.getInstance().send(udpMessage.getIp(), udpMessage.getPort(), MASTER_ELECTION + ":" + INFO_FM_RESPONSE + ":" + String.valueOf(lastBDUpdate));
                                break;
                            case INFO_FM_RESPONSE:
                                if (Utilities.ipAndPortToLong(udpMessage.getIp(), udpMessage.getPort()) > Utilities.ipAndPortToLong(masterIp, masterPort)) {
                                    setMaster(udpMessage.getIp(), udpMessage.getPort());
                                } else {
                                    setMaster(this.ipAdd, this.portNb);
                                }

                                //TODO update if needed
                                break;

                        }
                        break;
                    case USER_HEATHER:

                        Client clt;
                        switch (msg[1]) {
                            case COMMAND_ADD:
                                clt = gson.fromJson(msg[2], Client.class);
                                this.db.newClient(clt);
                                break;
                            case COMMAND_UPDATE:
                                clt = gson.fromJson(msg[2], Client.class);
                                this.db.updateClt(clt);
                                break;
                            case COMMAND_DELETE:
                                clt = gson.fromJson(msg[2], Client.class);
                                this.db.deleteClt(clt);
                                break;
                            case USER_GET_BY_PSEUDO:
                                clt = this.db.getClientByPseudo(msg[2]);
                                NetService.getInstance().send(udpMessage.getIp(), udpMessage.getPort(), USER_HEATHER + ":" + USER_GET_RESPONSE + ":" + gson.toJson(clt));
                                break;
                        }
                        this.lastBDUpdate = System.currentTimeMillis();
                        break;

                    case NODE_HEATHER:
                        Node node;
                        switch (msg[1]) {
                            case COMMAND_ADD:
                                node = gson.fromJson(msg[2], Node.class);
                                this.db.newNode(node);
                                break;
                            case COMMAND_UPDATE:
                                node = gson.fromJson(msg[2], Node.class);
                                this.db.updateNode(node);
                                break;
                            case COMMAND_DELETE:
                                node = gson.fromJson(msg[2], Node.class);
                                this.db.deleteNode(node);
                                break;
                            case NODE_GET_BY_NAME:
                                node = this.db.getNodeByName(msg[2]);
                                NetService.getInstance().send(udpMessage.getIp(), udpMessage.getPort(), USER_HEATHER + ":" + USER_GET_RESPONSE + ":" + gson.toJson(node));
                                break;

                        }
                        this.lastBDUpdate = System.currentTimeMillis();
                        break;
                    case USER_FRONT_HEATHER:
                        switch (msg[1]) {
                            case UF_AUTH:
                                boolean auth = authClt(msg[2], msg[3]);

                                if (auth) {
                                    NetService.getInstance().send(udpMessage.getIp(), udpMessage.getPort(), USER_FRONT_HEATHER + ":" + UF_AUTH + ":Success");
                                    clt = this.db.getClientByPseudo(msg[2]);

                                    //update if needed
                                    if (!(clt.getIp().equals(udpMessage.getIp()) && clt.getPort() == udpMessage.getPort())) {
                                        clt.setIp(udpMessage.getIp());
                                        clt.setPort(udpMessage.getPort());
                                        this.db.updateClt(clt);
                                        sendCommandToAllServers(USER_HEATHER + ":" + COMMAND_UPDATE + ":" + gson.toJson(clt));
                                        gossip = false;
                                    }

                                } else {
                                    NetService.getInstance().send(udpMessage.getIp(), udpMessage.getPort(), USER_FRONT_HEATHER + ":" + UF_AUTH + ":Failed");
                                }
                                break;
                        }
                        break;

                    default:
                        System.out.println("no msg received");
                        wait(1000);
                }


                //propagate throughout the network
                if (isMaster && gossip) {
                    sendCommandToAllServers(udpMessage.getMsg());
                }

                if (System.currentTimeMillis() - beginTimer >= MASTER_ELECTION_TIMER) {
                    electMaster();
                }

            }

        } catch (Exception e) {
        } finally {
            shutdown();
        }

    }

}
