package server;

import arreat.impl.core.NetService;

public class Server {

    private boolean shutdownRequested = false;


    public Server(){}

    private void init(){

    }

    private void start(){

        while(!this.shutdownRequested)
        {

        }
    }

    private void electMaster(){}

    private void shutdown(){

    }

    public void requestShutdown(){
        this.shutdownRequested = true;
    }

    public void sendACK(String ipAdd){
        NetService.getInstance().send();
    }




}
