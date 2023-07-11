package lk.uom.dc;

import lk.uom.dc.data.message.Request;

import java.io.IOException;

import static lk.uom.dc.log.LogManager.APP;

/**
 * Connects with the Bootstrap server
 */
public class BootstrapService {

    private final PeerServer peerServer;

    private final Peer bootstrap;

    public BootstrapService(PeerServer peerServer, Peer bootstrap) {
        this.peerServer = peerServer;
        this.bootstrap = bootstrap;
        bootstrap.setConnected(false);
    }

    public boolean register() {
        try {
            Request register = new Request(Request.Token.REG, peerServer.getSelf());
            peerServer.send(register, bootstrap);
            APP.info("Registering to bootstrap");
            return true;
        } catch (IOException ioException) {
            APP.error(ioException.getMessage(), ioException);
            return false;
        }
    }

    public boolean unRegister() {
        try {
            Request unregister = new Request(Request.Token.UNREG, peerServer.getSelf());

            // remove existing peers
            peerServer.setFirst(null);
            peerServer.setSecond(null);

            peerServer.send(unregister, bootstrap);

            APP.info("Unregistering from bootstrap");
            return true;
        } catch (IOException ioException) {
            APP.error(ioException.getMessage(), ioException);
            return false;
        }
    }
}
