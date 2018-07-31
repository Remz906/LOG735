package arreat.api.net;

import arreat.api.message.Message;

import java.net.DatagramPacket;
import java.util.List;

public interface PacketFactory {

    Packet make(Message message);
    List<Packet> make(Message message, int split);
    Packet make(DatagramPacket packet);
}
