package lk.uom.dc;

import lk.uom.dc.data.message.Heartbeat;
import lk.uom.dc.settings.Settings;

import java.io.IOException;
import java.time.LocalDateTime;

import static lk.uom.dc.log.LogManager.PING;

public class HeartbeatService implements Threadable {

    private final PeerServer server;

    private int firstFailureCount = 0;
    private int secondFailureCount = 0;

    public HeartbeatService(PeerServer server) {
        this.server = server;
    }

    private void ping(Peer peer) throws IOException {
        if (isEligible(peer)) {
            // create ping msg
            Heartbeat ping = new Heartbeat(Heartbeat.Token.PING, server.self);
            LocalDateTime now = LocalDateTime.now();

            NetAssist.send(ping, server.socket, peer.getSocket());
            peer.setLastPinged(now);
        }
    }

    public void pong(Heartbeat ping) throws IOException {
        Heartbeat pong = new Heartbeat(Heartbeat.Token.PONG, server.self);
        NetAssist.send(pong, server.socket, ping.sender().getSocket());
    }

    /**
     * Periodically sends ping and waits for pong.
     * <p>
     * Note that this should be invoked with a scheduled executor.
     */
    @Override
    public void run() {

        Peer first = server.first();
        Peer second = server.second();
        LocalDateTime now = LocalDateTime.now();

        if (
                null != first &&
                        null != first.getLastPinged() &&
                        first.getLastPinged().isBefore(now.minus(Settings.PING_INTERVAL))
        ) {
            // means we did not receive an immediate ping
            firstFailureCount++;

            if (firstFailureCount > Settings.FAILURE_COUNT) {
                PING.warn("lost peer: {}", server.first());
                server.setFirst(null);
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
                PING.warn("lost peer: {}", server.second());
                server.setSecond(null);
                secondFailureCount = 0;
            }
        }

        // send next ping
        try {
            ping(server.first());
        } catch (IOException exception) {
            PING.error(exception.getMessage(), exception);
        }

        try {
            ping(server.second());
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
}
