package lk.uom.dc;

import lk.uom.dc.data.message.Request;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketException;

import static lk.uom.dc.log.LogManager.APP;

/**
 * Connects with the Bootstrap server
 */
public class BootstrapClient {

    private final PeerServer peerServer;

    private final InetSocketAddress bootstrapAddress;

    public BootstrapClient(PeerServer peerServer, InetSocketAddress bootstrapAddress) throws SocketException {
        this.peerServer = peerServer;
        this.bootstrapAddress = bootstrapAddress;
    }

    public void register() throws IOException {
        Request register = new Request(Request.Token.REG, peerServer.getSelf());
        peerServer.send(register, bootstrapAddress);
        APP.info("Registering to bootstrap");
    }

    public void unRegister() throws IOException {
        Request unregister = new Request(Request.Token.UNREG, peerServer.getSelf());
        peerServer.send(unregister, bootstrapAddress);
        APP.info("Unregistering from bootstrap");
    }
}
