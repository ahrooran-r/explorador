package lk.uom.dc;

import lk.uom.dc.data.message.EchoOk;
import lk.uom.dc.data.message.Join;
import lk.uom.dc.data.message.RegOk;
import lk.uom.dc.data.message.UnRegOk;
import lk.uom.dc.settings.Settings;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.time.Duration;

import static lk.uom.dc.log.LogManager.APP;

public class MessageService implements MessageListener, Threadable {

    private final PeerServer server;

    private final HeartbeatService heartbeatService;

    private final BootstrapService bootstrapService;

    public MessageService(
            PeerServer server,
            HeartbeatService heartbeatService,
            BootstrapService bootstrapService
    ) {
        this.server = server;
        this.heartbeatService = heartbeatService;
        this.bootstrapService = bootstrapService;
    }

    @Override
    public void onMessage(String message, InetSocketAddress sender) throws IOException {
        // length -> 0, token -> 1
        Token token = Token.valueOf(message.split(Settings.FS)[1].toUpperCase());

        switch (token) {

            case REGOK -> {
                RegOk regOk = new RegOk(new Peer(sender, Settings.BOOTSTRAP_USERNAME));
                regOk.parseMessage(message);

                switch (regOk.getState()) {
                    case ALREADY_REGISTERED -> {
                        // somehow we got registered, but we do not have the state on our side
                        // unregister and register again
                        if (bootstrapService.unRegister()) {
                            bootstrapService.register();
                        }
                    }

                    // nothing we can do -> shutdown
                    case PORT_OCCUPIED -> System.exit(100);

                    case BS_FULL -> {
                        // wait for some time and try again
                        ThreadAssist.sleepQuiet(Duration.ofMinutes(3));
                        bootstrapService.register();
                    }

                    default -> {
                        if (null == server.getFirst()) server.setFirst(regOk.getFirst());
                        if (null == server.getSecond()) server.setSecond(regOk.getSecond());
                    }
                }
            }

            case UNROK -> {
                UnRegOk unRegOk = new UnRegOk();
                unRegOk.parseMessage(message);

                // remove existing peers -> doesn't matter if they are active or not
                server.setFirst(null);
                server.setSecond(null);
            }

            case ECHOK -> {
                EchoOk echoOk = new EchoOk();
                echoOk.parseMessage(message);
            }

            case PING -> heartbeatService.sendPong(sender);

            // what if I get a join message -> need to accept
            case JOIN -> {
                Join join = new Join();
                join.parseMessage(message);

                boolean canFirstAccept = null == server.getFirst() || !server.getFirst().isConnected();
                boolean canSecondAccept = null == server.getSecond() || !server.getSecond().isConnected();

                // if successful reply with a joinok message
                final Join joinOk = new Join(Join.Token.JOINOK, server.getSelf());

                if (canFirstAccept) {
                    server.setFirst(join.getSender());
                    server.getFirst().setConnected(true);
                    server.send(joinOk, join.getSender());

                } else if (canSecondAccept) {
                    server.setSecond(join.getSender());
                    server.getSecond().setConnected(true);
                    server.send(joinOk, join.getSender());
                } else {
                    // reject
                    Join reject = new Join(Join.Token.NOJOIN, server.getSelf());
                    server.send(reject, join.getSender());
                }
            }

            case JOINOK -> {
                Join joinOk = new Join();
                joinOk.parseMessage(message);

                boolean forFirst = joinOk.getSender().equals(server.getFirst());
                boolean forSecond = joinOk.getSender().equals(server.getSecond());
                if (forFirst && !server.getFirst().isConnected()) {
                    server.getFirst().setConnected(true);
                } else if (forSecond && !server.getSecond().isConnected()) {
                    server.getSecond().setConnected(true);
                }
            }

            case NOJOIN -> {
                Join joinReject = new Join();
                joinReject.parseMessage(message);

                boolean forFirst = joinReject.getSender().equals(server.getFirst());
                boolean forSecond = joinReject.getSender().equals(server.getSecond());
                if (forFirst) {
                    server.setFirst(null);
                } else if (forSecond) {
                    server.setSecond(null);
                }
            }
        }
    }

    @Override
    public void run() {
        // noinspection InfiniteLoopStatement
        while (true) {
            try {
                byte[] buffer = new byte[Settings.BOOTSTRAP_MSG_SIZE];
                DatagramPacket incoming = new DatagramPacket(buffer, buffer.length);
                server.getSocket().receive(incoming);
                onMessage(incoming);
            } catch (RuntimeException | IOException any) {
                APP.error(any.getMessage(), any);
            }
        }
    }

    @Override
    public String name() {
        return "message-listener";
    }

    @Override
    public int priority() {
        return Thread.NORM_PRIORITY + 2;
    }

    enum Token {

        REGOK("REGOK", "register command"),
        UNROK("UNROK", "unregister command"),
        ECHOK("ECHOK", "echo command"),

        PING("PING", "calling ping"),
        PONG("PONG", "echoing pong"),

        JOIN("JOIN", "join request"),
        JOINOK("JOIN", "accept join invite"),
        NOJOIN("NOJOIN", "reject join invite"),
        UNJOIN("UNJOIN", "unjoin request");

        public final String id;
        public final String description;

        Token(String id, String description) {
            this.id = id;
            this.description = description;
        }
    }
}
