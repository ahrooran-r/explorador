package lk.uom.dc;

import lk.uom.dc.data.Peer;
import lk.uom.dc.data.message.Message;
import lk.uom.dc.settings.Settings;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.net.*;

import static lk.uom.dc.log.LogManager.APP;

/**
 * A UDP server for peer. Modeled after the given BS server.
 */
public class PeerServer implements AutoCloseable {

    /**
     * This is the current peer itself.
     * This is used to transmit details of this peer to bootstrap and other peers
     */
    @Getter
    private final Peer self;

    @Getter
    private final DatagramSocket peerSocket;

    /**
     * Every peer connects to other 2 peers. I named them left and right for convenience.
     */
    @Getter
    @Setter
    private volatile Peer first;

    @Getter
    @Setter
    private volatile Peer second;

    public PeerServer(InetSocketAddress socketAddress, String username) throws SocketException {
        this.self = new Peer(socketAddress, username);
        this.peerSocket = new DatagramSocket(socketAddress);

        APP.info("Peer Server created at {}. Waiting for incoming data...", peerSocket.getPort());
    }

    @Override
    public void close() throws Exception {
        peerSocket.close();
    }

    /**
     * Returns response. Should move to common package since Peer server also handles this part.
     */
    public void send(Message message, SocketAddress to) throws IOException {
        NetAssist.send(message, peerSocket, to);
    }

    public static void main(String[] args) {
        try (
                PeerServer peerServer = new PeerServer(
                        new InetSocketAddress(Settings.PEER_HOST, Settings.PEER_PORT),
                        Settings.PEER_USERNAME)
        ) {
            //noinspection InfiniteLoopStatement
            while (true) {
                byte[] buffer = new byte[Settings.BOOTSTRAP_MSG_SIZE];
                DatagramPacket incoming = new DatagramPacket(buffer, buffer.length);
                peerServer.serverSocket.receive(incoming);

                bootstrap.onMessage(incoming);
            }
        } catch (IOException e) {
            APP.error(e.getMessage(), e);
        } catch (Exception e) {
            APP.error(e.getMessage(), e);
            System.exit(-1);
        }
    }

}
