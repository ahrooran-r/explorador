package lk.uom.dc;

import lk.uom.dc.data.message.PingPong;
import lk.uom.dc.settings.Settings;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.LocalDateTime;

import static lk.uom.dc.log.LogManager.PING;

public class HeartbeatService implements Threadable {

    private final PeerServer peerServer;

    private int firstFailureCount = 0;
    private int secondFailureCount = 0;

    public HeartbeatService(PeerServer peerServer) {
        this.peerServer = peerServer;
    }

    public void sendPong(InetSocketAddress to) throws IOException {
        PingPong pong = new PingPong(PingPong.Token.PONG, peerServer.getSelf());
        NetAssist.send(pong, peerServer.getSocket(), to);
    }

    /**
     * Periodically sends ping and waits for pong.
     * <p>
     * Note that this should be invoked with a scheduled executor.
     */
    @Override
    public void run() {

        Peer first = peerServer.getFirst();
        Peer second = peerServer.getSecond();
        LocalDateTime now = LocalDateTime.now();

        if (
                null != first &&
                        null != first.getLastPinged() &&
                        first.getLastPinged().isBefore(now.minus(Settings.PING_INTERVAL))
        ) {
            // means we did not receive an immediate ping
            firstFailureCount++;

            if (firstFailureCount > Settings.FAILURE_COUNT) {
                PING.warn("lost peer: {}", peerServer.getFirst());
                peerServer.setFirst(null);
                firstFailureCount = 0;
            }
        }

        if (null != second &&
                null != second.getLastPinged() &&
                second.getLastPinged().isBefore(now.minus(Settings.PING_INTERVAL))
        ) {
            // means we did not receive an immediate ping
            secondFailureCount++;

            if (secondFailureCount > Settings.FAILURE_COUNT) {
                PING.warn("lost peer: {}", peerServer.getSecond());
                peerServer.setSecond(null);
                secondFailureCount = 0;
            }
        }

        // send next ping
        try {
            sendPing(peerServer.getFirst());
        } catch (IOException exception) {
            PING.error(exception.getMessage(), exception);
        }

        try {
            sendPing(peerServer.getSecond());
        } catch (IOException exception) {
            PING.error(exception.getMessage(), exception);
        }
    }

    @Override
    public String name() {
        return "heartbeat-service";
    }

    @Override
    public int priority() {
        return Thread.NORM_PRIORITY;
    }

    /**
     * Is the peer eligible for heart beat ?
     */
    private boolean isEligible(Peer peer) {
        return null != peer &&
                // initially peer will not have the connected flag set
                // at the same, last ping time also will be 0
                (null == peer.getLastPinged() || peer.isConnected());
    }

    private void sendPing(Peer peer) throws IOException {
        if (isEligible(peer)) {
            // create ping msg
            PingPong ping = new PingPong(PingPong.Token.PING, peerServer.getSelf());
            LocalDateTime now = LocalDateTime.now();

            NetAssist.send(ping, peerServer.getSocket(), peer.getSocket());
            peer.setLastPinged(now);
        }
    }
}
