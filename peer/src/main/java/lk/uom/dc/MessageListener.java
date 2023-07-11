package lk.uom.dc;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

import static lk.uom.dc.log.LogManager.IN;

/**
 * Listens for messages from Peers and Bootstrap
 */
public interface MessageListener extends AbstractMessageListener {

    void onMessage(String message, InetSocketAddress sender) throws IOException;

    @Override
    default void onMessage(DatagramPacket packet) throws IOException {
        String raw = new String(packet.getData(), 0, packet.getLength(), StandardCharsets.UTF_8);
        IN.info("{} : {} - {}", packet.getAddress().getHostAddress(), packet.getPort(), raw);

        onMessage(raw, (InetSocketAddress) packet.getSocketAddress());
    }
}
