package server;

import arreat.DB.DatabaseMySQL;
import arreat.impl.core.NetService;
import arreat.impl.net.UDPMessage;
import sun.nio.ch.Net;

import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Server implements Runnable{

    private final int HB_TIMER = 10000;
    private final String HB_MSG_HEATHER = "HB";
    private final String HB_OK = "OK";
    private final String HB_DOWN = "DOWN";
    private final String HB_UNSTABLE = "UNSTABLE";

    private final int MASTER_ELECTION_TIMER = 500000;
    private final String INFO_HEATHER = "INFO";
    private final String INFO_IS_MASTER = "isMaster";
    private final String INFO_IS_MASTER_RESPONSE = "MR";
    private long beginTimer;
    private final int nbOfServers;
    private int nbOfAnswersServers;

    private final String USER_HEATHER = "USER";
    private final String USER_UPDATE = "UPDATE";
    private int LC = 0;



    private boolean shutdownRequested = false;
    private int portNb = 8080;
    private String ipAdd = "127.0.0.0";
    private DatabaseMySQL db;
    private boolean isMaster;
    private String masterIp;
    private int masterPort;
    private Thread hbThread;
    private Map<String, Integer> ipAddOfServers;




//
//    public Server(){}
//
//    public Server(int portNb){
//        this.portNb = portNb;
//    }
//
//    public Server(int portNb, String ipAdd){
//        this.ipAdd = ipAdd;
//        this.portNb = portNb;
//    }

    public Server(int portNb, String ipAdd, Map<String, Integer> ipOfServers){
        this.ipAdd = ipAdd;
        this.portNb = portNb;
        this.ipAddOfServers = ipOfServers;
        this.nbOfServers = ipOfServers.size();
    }

    private void init(){
        db = new DatabaseMySQL();
        electMaster();
        initHeartBeatThread();
    }


    private void electMaster(){
        try {
            sendCommandToAllServers(INFO_HEATHER+":"+INFO_IS_MASTER);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    private void shutdown(){
        this.db.disconnect();
    }

    private void sendCommandToAllServers(String command) throws UnknownHostException {
        for (Map.Entry<String, Integer> server : ipAddOfServers.entrySet()){
            NetService.getInstance().send(server.getKey(), server.getValue(), command);
        }
    }

    public void requestShutdown(){
        this.shutdownRequested = true;
    }

    public void initHeartBeatThread(){
        this.hbThread = new Thread(new Runnable() {
            public void run() {
                while(!shutdownRequested){
                    try {
                        Thread.sleep(HB_TIMER);
                        if (!isMaster){

                        }else if (masterIp != null){
                            NetService.getInstance().send(masterIp, masterPort, HB_MSG_HEATHER+":"+HB_OK);
                        }

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                    }

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
            while (!this.shutdownRequested){
                UDPMessage udpMessage = NetService.getInstance().receive();
                String[] msg = udpMessage.getMsg().split(":");
                switch(msg[0]){
                    case HB_MSG_HEATHER:
                        if (msg[1] == HB_OK){
                            sendACK(udpMessage.getIp(), udpMessage.getPort(), HB_MSG_HEATHER);
                        }else if (msg[1] == "ACK"){
                            beginTimer = System.currentTimeMillis();
                        }
                        break;
                    case INFO_HEATHER:
                        switch (msg[1]){
                            case INFO_IS_MASTER:
                                NetService.getInstance().send(udpMessage.getIp(), udpMessage.getPort(),INFO_HEATHER+":"+INFO_IS_MASTER_RESPONSE+":"+(isMaster?this.ipAdd:"False"));
                                break;
                            case INFO_IS_MASTER_RESPONSE:
                                if (msg[2] != "True"){
                                    masterIp = msg[2];
                                    beginTimer = System.currentTimeMillis();
                                }else{

                                    //just let the time pass so it auto elect itself;
                                }
                        }
                        break;
                    case USER_HEATHER:
                        switch (msg[1]){
                            case USER_UPDATE:

                                break;

                        }


                    default:
                        System.out.println("no msg received");
                        wait(1000);
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
