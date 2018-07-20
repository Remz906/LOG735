package server;

import arreat.DB.DatabaseMySQL;
import arreat.impl.core.NetService;

import java.net.UnknownHostException;

public class Server implements Runnable{

    private boolean shutdownRequested = false;
    private int portNb = 8080;
    private String ipAdd = "127.0.0.0";
    private DatabaseMySQL db;

    public Server(){}

    public Server(int portNb){
        this.portNb = portNb;
    }

    public Server(int portNb, String ipAdd){
        this.ipAdd = ipAdd;
        this.portNb = portNb;
    }

    private void init(){
        db = new DatabaseMySQL();
    }


    private void electMaster(){}

    private void shutdown(){
        this.db.disconnect();

    }

    public void requestShutdown(){
        this.shutdownRequested = true;
    }

    public void sendACK(String ipAdd, int portNb) throws UnknownHostException {
        NetService.getInstance().send(ipAdd, portNb, "ACK");
    }


    @Override
    public void run() {

        try {
            while (!this.shutdownRequested){
                String msg = NetService.getInstance().receive();
                switch(msg){
                    default:
                        System.out.println("no msg received");
                        wait(1000);
                }

            }

        }catch(Exception e ){}

        finally {
            shutdown();

        }

    }

}
