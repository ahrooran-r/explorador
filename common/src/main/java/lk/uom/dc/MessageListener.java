package lk.uom.dc;

import java.io.IOException;
import java.net.DatagramPacket;

public interface MessageListener {
    void onMessage(DatagramPacket message) throws IOException;
}
