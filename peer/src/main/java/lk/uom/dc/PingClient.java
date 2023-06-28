package lk.uom.dc;

import java.time.LocalDateTime;

public class PingClient implements Runnable {

    private final PeerServer peerServer;

    /**
     * last ping times of first and second pings
     */
    private LocalDateTime firstPing;
    private LocalDateTime secondPing;


    public PingClient(PeerServer peerServer) {
        this.peerServer = peerServer;
    }

    private void sendPing() {
    }

    /**
     * Periodically sends ping and waits for pong
     */
    @Override
    public void run() {

    }
}
