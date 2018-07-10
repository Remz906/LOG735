package arreat.api.net;

import arreat.api.message.Message;

import java.net.DatagramPacket;

public interface Packet {
    void setMessage(Message message);
    Message getMessage();
    void setAcknowledgmentHash(String hash);
    String getAcknowledgmentHash();
    boolean isAcknowledgmentRequired();
    boolean isFragmented();
    void setFragmentNumber(int fragment);
    int getFragmentNumber();
    DatagramPacket toDatagramPacket();
}
