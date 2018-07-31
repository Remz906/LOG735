package server;

import arreat.DB.Client;
import arreat.DB.DatabaseMySQL;
import arreat.DB.Node;
import arreat.impl.core.NetService;
import arreat.impl.net.UDPMessage;
import com.google.gson.Gson;

import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.List;

public class Server implements Runnable{

    private final int HB_TIMER = 10000;
    private final String HB_MSG_HEATHER = "HB";
    private final String HB_OK = "OK";
    private final String HB_DOWN = "DOWN";
    private final String HB_UNSTABLE = "UNSTABLE";

    private final int MASTER_ELECTION_TIMER = 500000;
    private final String MASTER_ELECTION = "ME";
    private final String MASTER_FIND_MASTER = "FIND_MASTER";
    private final String INFO_IS_MASTER = "isMaster";
    private final String INFO_FM_RESPONSE = "FMR";
    private long beginTimer;

    private final String USER_HEATHER = "USER";

    private final String USER_GET_BY_PSEUDO = "GET_BY_PSEUDO";
    private final String USER_GET_RESPONSE = "GR";

    private final String COMMAND_UPDATE = "UPDATE";
    private final String COMMAND_ADD = "ADD";
    private final String COMMAND_GOSSIP = "GOSSIP";
    private final String COMMAND_DELETE = "DELETE";



    private final String USER_FRONT_HEATHER = "UF";
    private final String UF_UPDATE_MASTER = "UM";


    private final String NODE_HEATHER = "NODE";


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

    private void init(){
        db = new DatabaseMySQL();
        electMaster();
        initHeartBeatThread();
    }


    private void electMaster(){
        try {
            masterIp = ipAdd;
            sendCommandToAllServers(MASTER_ELECTION +":"+MASTER_FIND_MASTER);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    private void setMaster(String ip, int portNb){
        this.ipAdd = ip;
        this.portNb = portNb;
        boolean preIsMaster = isMaster;
        isMaster = (ipAdd == masterIp);
        if (!preIsMaster && isMaster){
            try {
                sendCommandToAllClients(USER_FRONT_HEATHER +":" + UF_UPDATE_MASTER+":"+ipAdd+":"+String.valueOf(portNb));
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        }
    }

    private void shutdown(){
        this.db.disconnect();
    }

    private void sendCommandToAllServers(String command) throws UnknownHostException {
        for (Pair<String, Integer> server : ipAddOfServers){
            NetService.getInstance().send(server.getL(), server.getR(), command);
        }
    }

    private void sendCommandToAllClients(String command) throws UnknownHostException {
        List<Client> users = this.db.getAllClients();
        for (Client clt : users){
            NetService.getInstance().send(clt.getIp(),clt.getPort(), command);
        }
    }

    public void requestShutdown(){
        this.shutdownRequested = true;
    }

    private void initHeartBeatThread(){
        this.hbThread = new Thread(() -> {
            while(!shutdownRequested){
                try {
                    Thread.sleep(HB_TIMER);
                    if (masterIp != null && !isMaster){
                        NetService.getInstance().send(masterIp, masterPort, HB_MSG_HEATHER+":"+HB_OK);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }

            }
        });
    }

    public void sendACK(String ipAdd, int portNb, String heather) throws UnknownHostException {
        NetService.getInstance().send(ipAdd, portNb, heather+":"+"ACK");
    }



    @Override
    public void run() {
        try {
            this.hbThread.start();
            while (!this.shutdownRequested){
                UDPMessage udpMessage = NetService.getInstance().receive();
                Gson gson = new Gson();
                String[] msg = udpMessage.getMsg().split(":");
                switch(msg[0]){
                    case HB_MSG_HEATHER:
                        if (msg[1] == HB_OK){
                            sendACK(udpMessage.getIp(), udpMessage.getPort(), HB_MSG_HEATHER);
                        }else if (msg[1] == "ACK"){
                            beginTimer = System.currentTimeMillis();
                        }
                        break;
                    case MASTER_ELECTION:
                        switch (msg[1]){
                            case MASTER_FIND_MASTER:
                                if (Utilities.ipAndPortToLong(udpMessage.getIp(), udpMessage.getPort()) > Utilities.ipAndPortToLong(masterIp, masterPort))
                                {
                                    masterIp = udpMessage.getIp();
                                    masterPort = udpMessage.getPort();
                                    isMaster = false;
                                }
                                NetService.getInstance().send(udpMessage.getIp(), udpMessage.getPort(), MASTER_ELECTION +":"+ INFO_FM_RESPONSE);
                                break;
                            case INFO_FM_RESPONSE:
                                if (Utilities.ipAndPortToLong(udpMessage.getIp(), udpMessage.getPort()) > Utilities.ipAndPortToLong(masterIp, masterPort))
                                {
                                    masterIp = udpMessage.getIp();
                                    masterPort = udpMessage.getPort();
                                    isMaster = false;
                                }else{
                                    masterIp = this.ipAdd;
                                    masterPort = this.portNb;
                                    isMaster = true;
                                }

                                break;
                        }
                        break;
                    case USER_HEATHER:

                        Client clt;
                        switch (msg[1]){
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

                    case NODE_HEATHER:
                        Node node;
                        switch (msg[1]){
//                            case COMMAND_ADD:
//                                node = gson.fromJson(msg[2], Node.class);
//                                this.db.newClient(clt);
//                                break;
//                            case COMMAND_UPDATE:
//                                clt = gson.fromJson(msg[2], Client.class);
//                                this.db.updateClt(clt);
//                                break;
//                            case COMMAND_DELETE:
//                                clt = gson.fromJson(msg[2], Client.class);
//                                this.db.deleteClt(clt);
//                                break;
//                            case USER_GET_BY_PSEUDO:
//                                clt = this.db.getClientByPseudo(msg[2]);
//                                NetService.getInstance().send(udpMessage.getIp(), udpMessage.getPort(), USER_HEATHER + ":" + USER_GET_RESPONSE + ":" + gson.toJson(clt));
//                                break;
                        }

                    default:
                        System.out.println("no msg received");
                        wait(1000);
                }


                //propagate throughout the network
                if (isMaster){
                    sendCommandToAllServers(udpMessage.getMsg());
                }

                if (System.currentTimeMillis() - beginTimer >= MASTER_ELECTION_TIMER){
                    electMaster();
                }

            }

        }catch(Exception e ){}

        finally {
            shutdown();

        }

    }

}
