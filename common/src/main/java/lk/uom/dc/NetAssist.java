package lk.uom.dc;

import lk.uom.dc.data.message.Message;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import static lk.uom.dc.log.LogManager.OUT;

public class NetAssist {
    private NetAssist() {
    }

    /**
     * Returns response. Should move to common package since Peer server also handles this part.
     */
    public static void send(Message message, DatagramSocket from, SocketAddress to) throws IOException {
        Objects.requireNonNull(message);
        Objects.requireNonNull(to);

        String reply = message.toString();
        byte[] bytes = reply.getBytes(StandardCharsets.UTF_8);
        DatagramPacket packet = new DatagramPacket(bytes, bytes.length, to);
        from.send(packet);

        OUT.info("TO: {} MESSAGE{}", to, reply);
    }
}
