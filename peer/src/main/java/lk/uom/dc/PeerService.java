package lk.uom.dc;

import lk.uom.dc.data.message.Join;

import java.io.IOException;

import static lk.uom.dc.log.LogManager.APP;

/**
 * Handles joining with peers
 */
public class PeerService implements Threadable {

    private final PeerServer server;

    public PeerService(PeerServer server) {
        this.server = server;
    }

    private void join(Peer peer) {
        if (null != peer && !peer.isConnected()) {
            // as of now sender and maker are us
            Join join = new Join(Join.Token.JOIN, server.self);
            try {
                server.send(join, peer);
                APP.info("joining {}", peer);
            } catch (IOException ioException) {
                APP.error("cannot join with {}", peer, ioException);
                peer.setConnected(false);
            }
        }
    }

    @Override
    public void run() {
        join(server.getFirst());
        join(server.getSecond());
    }

    @Override
    public String name() {
        return "peer-service";
    }

    @Override
    public int priority() {
        return Thread.NORM_PRIORITY;
    }
}
