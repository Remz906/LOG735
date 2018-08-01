package server;

import arreat.db.Client;
import arreat.db.DatabaseMySQL;
import arreat.db.Node;
import arreat.impl.core.NetService;
import arreat.impl.net.UDPMessage;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Server implements Runnable {

    private static final Pattern MSG_PATTERN;

    private static final int HB_TIMER = 30000;
    private static final String HB_MSG_HEATHER = "HB";
    private static final String HB_OK = "OK";
    private static final String HB_DOWN = "DOWN";
    private static final String HB_UNSTABLE = "UNSTABLE";

    private static final int MASTER_ELECTION_TIMER = 10000;
    private static final String MASTER_ELECTION = "ME";
    private static final String MASTER_FIND_MASTER = "FIND_MASTER";
    private static final String INFO_IS_MASTER = "isMaster";
    private static final String INFO_FM_RESPONSE = "FMR";
    private long beginTimer;
    private long lastBDUpdate; //needed for restore

    private static final String USER_HEATHER = "USER";
    private static final String USER_GET_BY_USER = "GET_BY_USER";
    private static final String USER_GET_RESPONSE = "GR";

    private static final String NODE_HEATHER = "NODE";
    private static final String NODE_GET_BY_NAME = "GET_BY_NAME";

    private static final String COMMAND_UPDATE = "UPDATE";
    private static final String COMMAND_ADD = "ADD";
    private static final String COMMAND_GOSSIP = "GOSSIP";
    private static final String COMMAND_DELETE = "DELETE";
    private static final String COMMAND_UPDATE_ALL = "UPDATE_ALL";
    private static final String COMMAND_GET_BY_USERNAMES = "GET_BY_USERNAMES";


    private static final String UPDATE_MASTER = "UM";
    private static final String AUTH = "AUTH";


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
        NetService.getInstance().init();
        init();
    }

    private void init() {
        db = new DatabaseMySQL();
        lastBDUpdate = 0;
        electMaster();
        initHeartBeatThread();
    }


    private void electMaster() {
        System.out.println("Starting master election");
        try {
            masterIp = "127.0.0.0";
            masterPort = 0;
            sendCommandToAllServers(MASTER_ELECTION + ":" + MASTER_FIND_MASTER + ":" + String.valueOf(lastBDUpdate));
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    private void setMaster(String ip, int portNb) {
        if (!masterIp.equals(ip) || masterPort != portNb){
            beginTimer = System.currentTimeMillis();
            this.masterIp = ip;
            this.masterPort = portNb;
            System.out.println("New master elected " + masterIp + ":" + String.valueOf(masterPort));
            isMaster = (ipAdd.equals(masterIp));
            if (isMaster) {
                try {
                    sendCommandToAllClients(USER_HEATHER + ":" + UPDATE_MASTER + ":" + ipAdd + ":" + String.valueOf(portNb));
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    private void shutdown() {
        this.db.disconnect();
    }

    private void sendCommandToAllServers(String command) throws UnknownHostException {
        for (Pair<String, Integer> server : ipAddOfServers) {
            if (!ipAdd.equals(server.getL()) || portNb != server.getR())
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
                    if (masterIp != null  && !masterIp.equals("") && !isMaster) {
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

    private boolean authNode(String name, String pass) {
        Node node = this.db.getNodeByName(name);
        if (node != null){
            return node.getPwd().equals(pass);
        }
        return false;
    }

    private void updateDBIfNeeded(String ip, int portNb, long otherDbTime) throws UnknownHostException {

        long threshold = 1000;
        if (Math.abs(this.lastBDUpdate - otherDbTime) > threshold && lastBDUpdate > otherDbTime) {
            List<Client> updatedCltList = this.db.getAllClients();
            List<Node> updatedNodeList = this.db.getAllNode();
            Gson gson = new Gson();
            NetService.getInstance().send(ip, portNb, USER_HEATHER + ":" + COMMAND_UPDATE_ALL + ":" + gson.toJson(updatedCltList));
            NetService.getInstance().send(ip, portNb, NODE_HEATHER + ":" + COMMAND_UPDATE_ALL + ":" + gson.toJson(updatedNodeList));
        }
    }


    @Override
    public void run() {

        boolean gossip = true;
        try {
            this.hbThread.start();
            while (!this.shutdownRequested) {
                UDPMessage udpMessage = NetService.getInstance().receive();
                if (udpMessage != null) {


                    Gson gson = new Gson();
                    Matcher msg = MSG_PATTERN.matcher(udpMessage.getMsg()); //.split(":");
                    gossip = true;
                    if (msg.matches()) {
                        switch (msg.group(1)) {

                            case HB_MSG_HEATHER:
                                if (HB_OK.equals(msg.group(2))) {
                                    sendACK(udpMessage.getIp(), udpMessage.getPort(), HB_MSG_HEATHER);
                                } else if ("ACK".equals(msg.group(2))) {
                                    beginTimer = System.currentTimeMillis();
                                }
                                break;

                            case MASTER_ELECTION:
                                gossip = false;
                                msg = MSG_PATTERN.matcher(msg.group(2));

                                if (msg.matches()) {
                                    switch (msg.group(1)) {
                                        case MASTER_FIND_MASTER:
                                            if (Utilities.ipAndPortToLong(udpMessage.getIp(), udpMessage.getPort()) > Utilities.ipAndPortToLong(this.masterIp, this.masterPort)) {
                                                setMaster(udpMessage.getIp(), udpMessage.getPort());
                                            }
                                            NetService.getInstance().send(udpMessage.getIp(), udpMessage.getPort(), MASTER_ELECTION + ":" + INFO_FM_RESPONSE + ":" + String.valueOf(lastBDUpdate));
                                            updateDBIfNeeded(udpMessage.getIp(), udpMessage.getPort(), Long.parseLong(msg.group(2)));
                                            break;
                                        case INFO_FM_RESPONSE:
                                            if (Utilities.ipAndPortToLong(udpMessage.getIp(), udpMessage.getPort()) > Utilities.ipAndPortToLong(this.ipAdd, this.portNb)) {
                                                setMaster(udpMessage.getIp(), udpMessage.getPort());
                                            } else {
                                                setMaster(this.ipAdd, this.portNb);
                                            }
                                            updateDBIfNeeded(udpMessage.getIp(), udpMessage.getPort(), Long.parseLong(msg.group(2)));
                                            break;

                                    }
                                }
                                break;

                            case USER_HEATHER:
                                msg = MSG_PATTERN.matcher(msg.group(2));
                                Client clt;
                                List<Client> cltList;

                                if (msg.matches()) {
                                    switch (msg.group(1)) {
                                        case COMMAND_ADD:
                                            clt = gson.fromJson(msg.group(2), Client.class);
                                            clt.setIp(udpMessage.getIp());
                                            clt.setPort(udpMessage.getPort());
                                            if (this.db.getClientByPseudo(clt.getPseudo()) == null) {
                                                this.db.newClient(clt);
                                                this.lastBDUpdate = System.currentTimeMillis();
                                                NetService.getInstance().send(udpMessage.getIp(), udpMessage.getPort(), USER_HEATHER + ":" + AUTH + ":" + gson.toJson(clt));
                                            } else {
                                                NetService.getInstance().send(udpMessage.getIp(), udpMessage.getPort(), USER_HEATHER + ":" + AUTH + ":Failed");
                                            }
                                            break;
                                        case COMMAND_UPDATE:
                                            clt = gson.fromJson(msg.group(2), Client.class);
                                            clt.setIp(udpMessage.getIp());
                                            clt.setPort(udpMessage.getPort());
                                            this.db.updateClt(clt);
                                            this.lastBDUpdate = System.currentTimeMillis();
                                            this.sendACK(udpMessage.getIp(), udpMessage.getPort(), USER_HEATHER);
                                            break;
                                        case COMMAND_UPDATE_ALL:
                                            cltList = gson.fromJson(msg.group(2), new TypeToken<List<Client>>() {
                                            }.getType());
                                            this.db.updateAllClt(cltList);
                                            this.lastBDUpdate = System.currentTimeMillis();
                                            this.sendACK(udpMessage.getIp(), udpMessage.getPort(), USER_HEATHER);
                                            break;
                                        case COMMAND_DELETE:
                                            clt = gson.fromJson(msg.group(2), Client.class);
                                            this.db.deleteClt(clt);
                                            this.lastBDUpdate = System.currentTimeMillis();
                                            this.sendACK(udpMessage.getIp(), udpMessage.getPort(), USER_HEATHER);
                                            break;
                                        case USER_GET_BY_USER:
                                            clt = this.db.getClientByPseudo(msg.group(2));
                                            NetService.getInstance().send(udpMessage.getIp(), udpMessage.getPort(), USER_HEATHER + ":" + USER_GET_RESPONSE + ":" + gson.toJson(clt));
                                            break;
                                        case COMMAND_GET_BY_USERNAMES:
                                            List<String> cltNameList = gson.fromJson(msg.group(2), new TypeToken<List<String>>() {
                                            }.getType());
                                            cltList = this.db.getCltsByUsername(cltNameList);
                                            NetService.getInstance().send(udpMessage.getIp(), udpMessage.getPort(), USER_HEATHER + ":" + COMMAND_GET_BY_USERNAMES + ":" + gson.toJson(cltList));
                                            break;

                                        case AUTH:
                                            String[] msgSplit = msg.group(2).split(":");

                                            boolean auth = authClt(msgSplit[0], msgSplit[1]);

                                            if (auth) {
                                                clt = this.db.getClientByPseudo(msgSplit[0]);

                                                //update if needed
                                                if (!(clt.getIp().equals(udpMessage.getIp()) && clt.getPort() == udpMessage.getPort())) {
                                                    clt.setIp(udpMessage.getIp());
                                                    clt.setPort(udpMessage.getPort());
                                                    this.db.updateClt(clt);
                                                    NetService.getInstance().send(udpMessage.getIp(), udpMessage.getPort(), USER_HEATHER + ":" + AUTH + ":" + gson.toJson(clt));
                                                    sendCommandToAllServers(USER_HEATHER + ":" + COMMAND_UPDATE + ":" + gson.toJson(clt));
                                                    gossip = false;
                                                }

                                            } else {
                                                NetService.getInstance().send(udpMessage.getIp(), udpMessage.getPort(), USER_HEATHER + ":" + AUTH + ":Failed");
                                            }
                                            break;
                                    }
                                }
                                break;

                                case NODE_HEATHER:
                                    Node node;
                                    msg = MSG_PATTERN.matcher(msg.group(2));

                                    if (msg.matches()) {

                                        switch (msg.group(1)) {
                                            case COMMAND_ADD:
                                                node = gson.fromJson(msg.group(2), Node.class);
                                                this.db.newNode(node);
                                                this.sendACK(udpMessage.getIp(), udpMessage.getPort(), NODE_HEATHER);
                                                break;
                                            case COMMAND_UPDATE:
                                                node = gson.fromJson(msg.group(2), Node.class);
                                                this.db.updateNode(node);
                                                this.sendACK(udpMessage.getIp(), udpMessage.getPort(), NODE_HEATHER);
                                                break;
                                            case COMMAND_UPDATE_ALL:
                                                List<Node> nodeList = gson.fromJson(msg.group(2), new TypeToken<List<Node>>() {
                                                }.getType());
                                                this.db.updateAllNode(nodeList);
                                                this.sendACK(udpMessage.getIp(), udpMessage.getPort(), NODE_HEATHER);
                                                break;
                                            case COMMAND_DELETE:
                                                node = gson.fromJson(msg.group(2), Node.class);
                                                this.db.deleteNode(node);
                                                this.sendACK(udpMessage.getIp(), udpMessage.getPort(), NODE_HEATHER);
                                                break;
                                            case NODE_GET_BY_NAME:
                                                node = this.db.getNodeByName(msg.group(2));
                                                NetService.getInstance().send(udpMessage.getIp(), udpMessage.getPort(), USER_HEATHER + ":" + USER_GET_RESPONSE + ":" + gson.toJson(node));
                                                break;
                                            case AUTH:
                                                //2 = roomName 3 = pass
                                                String[] msgSplit = msg.group(2).split(":");

                                                boolean auth = authNode(msgSplit[0], msgSplit[1]);
                                                if (auth) {
                                                    node = this.db.getNodeByName(msgSplit[0]);
                                                    Client masterClt = this.db.getClientByPseudo(node.getMasterUser());
                                                    NetService.getInstance().send(udpMessage.getIp(), udpMessage.getPort(), NODE_HEATHER + ":" + AUTH + ":" + gson.toJson(masterClt));


                                                    //get new client
                                                    clt = this.db.getClientByIp(udpMessage.getIp());

                                                    //should be added to db here
                                                    /////


                                                    NetService.getInstance().send(masterClt.getIp(), masterClt.getPort(), NODE_HEATHER + ":" + node.getName() + ":" + gson.toJson(clt));


                                                } else if (this.db.getNodeByName(msgSplit[0]) == null) {
                                                    clt = this.db.getClientByIp(udpMessage.getIp());
                                                    node = new Node(msgSplit[0], clt.getPseudo(), msgSplit[1]);
                                                    this.db.newNode(node);
                                                    NetService.getInstance().send(udpMessage.getIp(), udpMessage.getPort(), NODE_HEATHER + ":" + "CREATED" + ":" + msgSplit[0]);
                                                } else {
                                                    NetService.getInstance().send(udpMessage.getIp(), udpMessage.getPort(), USER_HEATHER + ":" + AUTH + ":Failed");
                                                }

                                                break;

                                        }
                                    }
                                    this.lastBDUpdate = System.currentTimeMillis();
                                    break;

                                }
                        }


                        //propagate throughout the network
                        if (isMaster && gossip) {
                            sendCommandToAllServers(udpMessage.getMsg());
                        }
                    } else {
                        System.out.println("no msg received");
                        Thread.sleep(1000);
                    }

                    if (System.currentTimeMillis() - beginTimer >= MASTER_ELECTION_TIMER ||
                            (udpMessage != null && Utilities.ipAndPortToLong(this.masterIp, this.masterPort) < Utilities.ipAndPortToLong(udpMessage.getIp(), udpMessage.getPort()))) {
                        System.out.println("master connection lost");
                        electMaster();
                    }

                }
            } catch(Exception e){
                System.out.println(e);
            } finally{
                shutdown();
            }
    }

    static {
        MSG_PATTERN = Pattern.compile("(\\w+):(.+)");
    }
}
