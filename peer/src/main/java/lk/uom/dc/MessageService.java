package lk.uom.dc;

import lk.uom.dc.data.message.*;
import lk.uom.dc.settings.Settings;

import java.io.IOException;
import java.net.DatagramPacket;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import static lk.uom.dc.log.LogManager.APP;

public class MessageService implements AbstractMessageListener, Threadable {

    private final PeerServer server;

    private final HeartbeatService heartbeatService;

    private final BootstrapService bootstrapService;

    private final SearchService searchService;

    public MessageService(
            PeerServer server,
            HeartbeatService heartbeatService,
            BootstrapService bootstrapService,
            SearchService searchService
    ) {
        this.server = server;
        this.heartbeatService = heartbeatService;
        this.bootstrapService = bootstrapService;
        this.searchService = searchService;
    }

    @Override
    public void onMessage(DatagramPacket message) throws IOException {
        // length -> 0, token -> 1
        String raw = new String(message.getData(), 0, message.getLength(), StandardCharsets.UTF_8);
        Token token = Token.valueOf(raw.split(Settings.FS)[1].toUpperCase());

        switch (token) {

            case REGOK -> {
                RegOk regOk = new RegOk();
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

            case PING -> {
                Heartbeat heartbeat = new Heartbeat();
                heartbeat.parseMessage(message);
                heartbeatService.replyPong(heartbeat);
            }

            // what if I get a join message -> need to accept
            case JOIN -> {
                Join join = new Join();
                join.parseMessage(message);

                boolean canFirstAccept = null == server.getFirst() || !server.getFirst().isConnected();
                boolean canSecondAccept = null == server.getSecond() || !server.getSecond().isConnected();

                // if successful reply with a joinok message
                final Join joinOk = new Join(Join.Token.JOINOK, server.self);

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
                    Join reject = new Join(Join.Token.ERROR, server.self);
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

            case LEAVE -> {
                Join leave = new Join();
                leave.parseMessage(message);

                boolean forFirst = leave.getSender().equals(server.getFirst());
                boolean forSecond = leave.getSender().equals(server.getSecond());
                if (forFirst) {
                    server.setFirst(null);
                } else if (forSecond) {
                    server.setSecond(null);
                }
                
                // send leave ok message
                Join leaveOk = new Join(Join.Token.LEAVEOK, server.self);
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
                server.socket.receive(incoming);
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

        JOIN("JOIN", "join with me"),
        LEAVE("LEAVE", "leave me"),
        JOINOK("JOINOK", "accept join invite"),
        LEAVEOK("LEAVEOK", "leave accepted");

        public final String id;
        public final String description;

        Token(String id, String description) {
            this.id = id;
            this.description = description;
        }
    }
}
