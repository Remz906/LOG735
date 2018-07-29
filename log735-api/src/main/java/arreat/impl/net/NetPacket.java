package arreat.impl.net;

import arreat.api.message.Body;
import arreat.api.message.Message;
import arreat.api.net.Packet;

import java.net.DatagramPacket;

public class NetPacket implements Packet {

    private int fragmentNumber;
    private String hash;
    private Message message;
    private String messageImplementationName;
    private String bodyImplementationName;

    public NetPacket() {
        this.fragmentNumber = -1;
    }

    @Override
    public void setMessage(Message message) {
        if (message == null || message.getBody() == null || message.getTarget() == null) {
            throw new IllegalArgumentException("Cannot set UDPMessage, Invalid UDPMessage received.");
        }
        this.message = message;
        this.messageImplementationName = message.getClass().getName();
        this.bodyImplementationName = message.getBody().getClass().getName();
    }

    @Override
    public Message getMessage() {
        return this.message;
    }

    @Override
    public void setAcknowledgmentHash(String hash) {
        this.hash = hash;
    }

    @Override
    public String getAcknowledgmentHash() {
        return this.hash;
    }

    @Override
    public boolean isAcknowledgmentRequired() {
        return this.hash != null;
    }

    @Override
    public boolean isFragmented() {
        return this.fragmentNumber >= 0;
    }

    @Override
    public void setFragmentNumber(int fragment) {
        this.fragmentNumber = fragment;
    }

    @Override
    public int getFragmentNumber() {
        return this.fragmentNumber;
    }

    @Override
    public DatagramPacket toDatagramPacket() {
        if (this.message == null) {
            throw new UnsupportedOperationException("A message must be set in order to create a datagram packet.");
        }
        byte[] data = new NetPacketBody(this).toJson().getBytes();

        return new DatagramPacket(data, data.length, this.message.getTarget().getAddress());
    }

    public static class NetPacketBody implements Body {

        private Integer fragmentNumber;
        private String hash;
        private String body;
        private String messageImplementationName;
        private String bodyImplementationName;

        public NetPacketBody() {

        }

        private NetPacketBody(NetPacket packet) {
            this.fragmentNumber = packet.fragmentNumber < 0 ? null : packet.fragmentNumber;
            this.hash = packet.hash;
            this.body = packet.message.getBody().toJson();
            this.messageImplementationName = packet.messageImplementationName;
            this.bodyImplementationName = packet.bodyImplementationName;
        }

        public String getMessageImplementationName() {
            return messageImplementationName;
        }

        public String getBodyImplementationName() {
            return bodyImplementationName;
        }

        public String getBody() {
            return body;
        }

        public String getHash() {
            return hash;
        }

        public Integer getFragmentNumber() {
            return fragmentNumber;
        }
    }
}
