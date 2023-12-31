package lk.uom.dc;

import lk.uom.dc.data.message.Message;
import lk.uom.dc.settings.Settings;
import lombok.Setter;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static lk.uom.dc.log.LogManager.APP;

/**
 * A UDP server for peer. Modeled after the given BS server.
 */
public class PeerServer implements AutoCloseable {

    /**
     * This is the current peer itself.
     * This is used to transmit details of this peer to bootstrap and other peers
     */
    public final Peer self;

    public final DatagramSocket socket;

    /**
     * Every peer connects to other 2 peers. I named them first and second for convenience.
     */
    @Setter
    private volatile Peer first;

    @Setter
    private volatile Peer second;

    public PeerServer(InetSocketAddress socketAddress, String username) throws SocketException {
        this.self = new Peer(socketAddress, username);
        this.socket = new DatagramSocket(socketAddress);

        APP.info("Peer Server created at {}. Waiting for incoming data...", socket.getPort());
    }

    public static void main(String[] args) {
        try (
                PeerServer peerServer = new PeerServer(
                        new InetSocketAddress(Settings.PEER_HOST, Settings.PEER_PORT),
                        Settings.PEER_USERNAME
                )
        ) {

            // (A) setup threads

            // 1. Peer service
            PeerService join = new PeerService(peerServer);
            ScheduledExecutorService peerService = Executors.newSingleThreadScheduledExecutor(
                    Thread.ofVirtual()
                            .name(join.name())
                            .factory()
            );
            Runtime.getRuntime().addShutdownHook(new Thread(peerService::close));

            // 1. heartbeat / ping service
            HeartbeatService heartbeat = new HeartbeatService(peerServer);
            ScheduledExecutorService heartBeatService = Executors.newSingleThreadScheduledExecutor(
                    Thread.ofVirtual()
                            .name(heartbeat.name())
                            .factory()
            );
            Runtime.getRuntime().addShutdownHook(new Thread(heartBeatService::close));

            // 2. bootstrap client
            Peer bootstrap = new Peer(
                    new InetSocketAddress(Settings.BOOTSTRAP_HOST, Settings.BOOTSTRAP_PORT),
                    Settings.BOOTSTRAP_USERNAME
            );
            BootstrapService bootstrapService = new BootstrapService(peerServer, bootstrap);

            // 3. Search Service
            SearchService searchService = new SearchService(peerServer);

            // 3. incoming message handler
            MessageService messageService = new MessageService(
                    peerServer,
                    heartbeat,
                    bootstrapService,
                    searchService
            );
            Thread listenerService = Thread.ofVirtual()
                    .name(messageService.name())
                    .unstarted(messageService);
            listenerService.setPriority(messageService.priority());

            // (B) start threads

            // 1. start message handler first. this handles incoming messages.
            listenerService.start();
            APP.info("message listener service init");

            // 2. start join thread
            peerService.scheduleWithFixedDelay(
                    join,
                    0,
                    Settings.PING_INTERVAL.toMillis(),
                    TimeUnit.MILLISECONDS
            );
            APP.info("peer join service init");

            // 3. start heart beat
            heartBeatService.scheduleWithFixedDelay(
                    heartbeat,
                    0,
                    Settings.PING_INTERVAL.toMillis(),
                    TimeUnit.MILLISECONDS
            );
            APP.info("heartbeat service init");

            // (C) Connect to bootstrap and get first set of peers
            bootstrapService.register();

            while (true) {
            }

        } catch (IOException e) {
            APP.error(e.getMessage(), e);

        } catch (Exception e) {
            APP.error("Irrecoverable exception occured", e);
            System.exit(-1);
        }
    }

    @Override
    public void close() {
        socket.close();
    }

    /**
     * Returns response. Should move to common package since Peer server also handles this part.
     */
    public void send(Message<?> message, SocketAddress to) throws IOException {
        NetAssist.send(message, socket, to);
    }

    /**
     * send messages to other peers
     */
    public void send(Message<?> message, Peer to) throws IOException {
        send(message, to.getSocket());
    }

    /**
     * Reply to an incoming message
     */
    public void reply(Message<?> reply, Message<?> to) throws IOException {
        send(reply, to.sender());
    }

    public Peer first() {
        return this.first;
    }

    public Peer second() {
        return this.second;
    }
}
