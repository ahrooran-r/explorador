package lk.uom.dc;

import lk.uom.dc.data.Peer;
import lk.uom.dc.data.message.*;
import lk.uom.dc.settings.Settings;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;

import static lk.uom.dc.log.LogManager.APP;

public class BootstrapServer implements BootstrapMessageListener, AutoCloseable {

    private final DatagramSocket serverSocket;

    /**
     * bootstrap server itself is considered a peer
     */
    private final Peer self;

    /**
     * Holds the list of current peers in the network.
     * <p>
     * Not thread safe. Use locks when necessary.
     */
    private final List<Peer> peers;

    public BootstrapServer(InetSocketAddress socketAddress) throws SocketException {

        // assume only 1 bootstrap is available
        this.self = new Peer(socketAddress, "bootstrap");

        serverSocket = new DatagramSocket(socketAddress);
        peers = new ArrayList<>(16);
        APP.info("Bootstrap Server created at {}. Waiting for incoming data...", serverSocket.getPort());
    }

    public static void main(String[] args) {
        try (
                BootstrapServer bootstrap = new BootstrapServer(
                        new InetSocketAddress(Settings.BOOTSTRAP_HOST, Settings.BOOTSTRAP_PORT))
        ) {
            //noinspection InfiniteLoopStatement
            while (true) {
                byte[] buffer = new byte[Settings.BOOTSTRAP_MSG_SIZE];
                DatagramPacket incoming = new DatagramPacket(buffer, buffer.length);
                bootstrap.serverSocket.receive(incoming);

                bootstrap.onMessage(incoming);
            }
        } catch (IOException e) {
            APP.error(e.getMessage(), e);
        } catch (Exception e) {
            APP.error(e.getMessage(), e);
            System.exit(-1);
        }
    }

    /**
     * Handles incoming messages from Peers and responds accordingly.
     */
    @Override
    public void onMessage(Request request) throws IOException {
        switch (request.getToken()) {
            case REG -> handleReg(request);
            case UNREG -> handleUnReg(request);
            case ECHO -> {
                // small code so I didn't move to a separate method
                for (Peer(InetSocketAddress socket, String username) : peers) {
                    APP.info("IP: {} PORT: {} USER_NAME:{}",
                            socket.getAddress().getHostAddress(),
                            socket.getPort(),
                            username
                    );
                }
                EchoOk echoOk = new EchoOk(EchoOk.Token.SUCCESSFUL, self);
                reply(echoOk, request.getSender().address());
            }
        }
    }

    /**
     * Handles register request.
     */
    private void handleReg(Request request) throws IOException {

        final Peer joinee = request.getSender();
        RegOk regOk = null;

        if (peers.isEmpty()) {
            regOk = new RegOk(null, null);
            peers.add(joinee);

        } else {
            boolean isOkay = true;

            for (Peer(InetSocketAddress socket, String username) : peers) {
                if (socket.getPort() == joinee.address().getPort()) {
                    if (username.equalsIgnoreCase(joinee.username())) {
                        regOk = new RegOk(RegOk.Token.ALREADY_REGISTERED, self);
                    } else {
                        regOk = new RegOk(RegOk.Token.PORT_OCCUPIED, self);
                    }
                    isOkay = false;
                }
            }

            if (isOkay) {
                switch (peers.size()) {
                    case 1 -> regOk = new RegOk(peers.get(0), null, self);
                    case 2 -> regOk = new RegOk(peers.get(0), peers.get(0), self);
                    default -> {
                        Random r = ThreadLocalRandom.current();
                        int first = r.nextInt(peers.size());
                        int second;
                        do {
                            second = r.nextInt(peers.size());
                        } while (first == second);

                        APP.debug("first: {}, second: {}", first, second);
                        regOk = new RegOk(peers.get(first), peers.get(second), self);
                    }
                }

                peers.add(joinee);
            }
        }

        reply(regOk, joinee.address());
    }

    /**
     * Handles unregister request.
     */
    private void handleUnReg(Request request) {

        Peer joinee = request.getSender();

        Predicate<Peer> replyAndRemove = peer -> {
            boolean shouldRemove = peer.address().getPort() == joinee.address().getPort();

            if (shouldRemove) {
                UnRegOk unregOk = new UnRegOk(UnRegOk.Token.SUCCESS, self);
                try {
                    reply(unregOk, joinee.address());
                } catch (IOException ioException) {
                    APP.error("could not send reply: {}", unregOk.toString(), ioException);
                }
            }
            return shouldRemove;
        };

        peers.removeIf(replyAndRemove);
    }

    /**
     * Returns response. Should move to common package since Peer server also handles this part.
     */
    private void reply(Message message, SocketAddress to) throws IOException {
        NetAssist.send(message, serverSocket, to);
    }

    @Override
    public void close() throws Exception {
        serverSocket.close();
    }
}