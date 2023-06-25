package lk.uom.dc;

import lk.uom.dc.data.*;
import lk.uom.dc.data.message.*;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;

import static lk.uom.dc.log.LogManager.*;

public class BootstrapServer {

    public static void main(String[] args) {
        List<Peer> nodes = new ArrayList<>(100);

        try (
                DatagramSocket sock = new DatagramSocket(55555)
        ) {

            APP.info("Bootstrap Server created at {}. Waiting for incoming data...", sock.getPort());

            //noinspection InfiniteLoopStatement
            while (true) {

                byte[] buffer = new byte[65536];
                DatagramPacket incoming = new DatagramPacket(buffer, buffer.length);
                sock.receive(incoming);

                String raw = new String(incoming.getData(), 0, incoming.getLength(), StandardCharsets.UTF_8);
                IN.info("{} : {} - {}", incoming.getAddress().getHostAddress(), incoming.getPort(), raw);

                Request request = new Request();
                request.parseMessage(raw);

                // this is the peer that wants to join
                Peer joinee = new Peer(request.getSender(), request.getUsername());

                switch (request.getToken()) {

                    case REG -> {
                        RegOk regOk = null;

                        if (nodes.isEmpty()) {
                            regOk = new RegOk(null, null);
                            nodes.add(joinee);

                        } else {
                            boolean isOkay = true;

                            for (Peer(InetSocketAddress socket, String username) : nodes) {
                                if (socket.getPort() == joinee.socket().getPort()) {
                                    if (username.equalsIgnoreCase(joinee.username())) {
                                        regOk = new RegOk(RegOk.Token.ALREADY_REGISTERED);
                                    } else {
                                        regOk = new RegOk(RegOk.Token.PORT_OCCUPIED);
                                    }
                                    isOkay = false;
                                }
                            }

                            if (isOkay) {
                                switch (nodes.size()) {
                                    case 1 -> regOk = new RegOk(nodes.get(0), null);
                                    case 2 -> regOk = new RegOk(nodes.get(0), nodes.get(0));
                                    default -> {
                                        Random r = ThreadLocalRandom.current();
                                        int first = r.nextInt(nodes.size());
                                        int second;
                                        do {
                                            second = r.nextInt(nodes.size());
                                        } while (first == second);

                                        APP.debug("first: {}, second: {}", first, second);
                                        regOk = new RegOk(nodes.get(first), nodes.get(second));
                                    }
                                }

                                nodes.add(joinee);
                            }
                        }

                        sendResponse(regOk, sock, incoming.getSocketAddress());
                    }

                    case UNREG -> {

                        Predicate<Peer> replyAndRemove = peer -> {
                            boolean shouldRemove = peer.socket().getPort() == joinee.socket().getPort();

                            if (shouldRemove) {
                                UnRegOk unregOk = new UnRegOk(UnRegOk.Token.NO_NODES);
                                try {
                                    sendResponse(unregOk, sock, incoming.getSocketAddress());
                                } catch (IOException ioException) {
                                    APP.error("could not send reply: {}", unregOk.toString(), ioException);
                                }
                            }
                            return shouldRemove;
                        };

                        nodes.removeIf(replyAndRemove);
                    }

                    case ECHO -> {
                        for (Peer(InetSocketAddress socket, String username) : nodes) {
                            APP.info("IP: {} PORT: {} USER_NAME:{}",
                                    socket.getAddress().getHostAddress(),
                                    socket.getPort(),
                                    username
                            );
                        }
                        EchoOk echoOk = new EchoOk(EchoOk.Token.SUCCESSFUL);
                        sendResponse(echoOk, sock, incoming.getSocketAddress());
                    }
                }
            }
        } catch (IOException e) {
            APP.error(e.getMessage(), e);
        }
    }

    private static void sendResponse(Message message, DatagramSocket from, SocketAddress to) throws IOException {
        Objects.requireNonNull(message);
        Objects.requireNonNull(from);
        Objects.requireNonNull(to);

        String reply = message.toString();
        byte[] bytes = reply.getBytes(StandardCharsets.UTF_8);
        DatagramPacket packet = new DatagramPacket(bytes, bytes.length, to);
        from.send(packet);

        OUT.info("IP: {} MESSAGE{}", to, reply);
    }
}
