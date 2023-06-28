package lk.uom.dc;

import lk.uom.dc.data.message.Request;

import java.io.IOException;
import java.net.DatagramPacket;

/**
 * Listens for messages from Bootstrap
 */
public interface BootstrapMessageListener extends MessageListener {

    void onMessage(Request request) throws IOException;

    @Override
    default void onMessage(DatagramPacket message) throws IOException {
        Request request = new Request();
        request.parseMessage(message);
        onMessage(request);
    }
}
