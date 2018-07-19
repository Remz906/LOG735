package server;

import arreat.impl.DataBase;
import arreat.impl.core.NetService;

import java.net.UnknownHostException;

public class Server implements Runnable{

    private boolean shutdownRequested = false;
    private int portNb = 8080;
    private DataBase db;

    public Server(){}

    public Server(int portNb){
        this.portNb = portNb;
    }

    private void init(){

    }


    private void electMaster(){}

    private void shutdown(){

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

            }

        }catch(Exception e ){}

        finally {
            shutdown();

        }


    }
}
