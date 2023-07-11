package lk.uom.dc;

import java.io.IOException;
import java.net.DatagramPacket;

public interface AbstractMessageListener {
    void onMessage(DatagramPacket message) throws IOException;
}
