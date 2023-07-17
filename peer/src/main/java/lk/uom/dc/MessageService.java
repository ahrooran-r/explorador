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
                        if (null == server.first()) server.setFirst(regOk.getFirst());
                        if (null == server.second()) server.setSecond(regOk.getSecond());
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
                // there is nothing to do about this
            }

            // what if I get a join message -> need to accept
            case JOIN -> {

                Join joinMsg = new Join();
                joinMsg.parseMessage(message);

                boolean forFirst = joinMsg.sender().fuzzyEquals(server.first());
                boolean forSecond = joinMsg.sender().fuzzyEquals(server.second());

                switch (joinMsg.getType()) {
                    case JOIN -> {
                        // if successful reply with a joinok message
                        final Join joinOk = new Join(Join.Token.JOINOK, server.self);

                        if (null == server.first()) {
                            server.setFirst(joinMsg.sender());
                            // server.first().setConnected(true);
                            server.reply(joinOk, joinMsg);

                        } else if (null == server.second()) {
                            server.setSecond(joinMsg.sender());
                            // server.second().setConnected(true);
                            server.reply(joinOk, joinMsg);
                        } else {

                            // suppose first and second are already set
                            // but not connected and waiting for join ok message

                            if (!forFirst && !forSecond) {
                                // then reject
                                Join reject = new Join(Join.Token.ERROR, server.self);
                                server.reply(reject, joinMsg);
                            }
                        }
                    }

                    case JOINOK -> {
                        if (forFirst && !server.first().isConnected()) {
                            server.first().setConnected(true);
                        } else if (forSecond && !server.second().isConnected()) {
                            server.second().setConnected(true);
                        }
                    }

                    case LEAVE -> {
                        if (forFirst) server.first().setConnected(false);
                        else if (forSecond) server.second().setConnected(false);

                        // send leave ok message
                        Join leaveOk = new Join(Join.Token.LEAVEOK, Join.Token.SUCCESS, server.self);
                        server.reply(leaveOk, joinMsg);
                    }

                    case LEAVEOK -> {
                        if (forFirst) server.setFirst(null);
                        else if (forSecond) server.setSecond(null);
                    }
                }

            }


            case PING -> {
                Heartbeat heartbeat = new Heartbeat();
                heartbeat.parseMessage(message);
                heartbeatService.pong(heartbeat);
            }

            case PONG -> {
                // don't know what to do
            }

            case SER -> {

                Search search = new Search();
                search.parseMessage(message);

                if (search.hops() > Settings.MAX_HOPS) return;

                var result = searchService.search(search.query());
                if (result.isEmpty()) {
                    // pass it to peers
                    search.setHops(search.hops() + 1);
                    searchService.pass(search);

                } else {
                    // return response
                    SearchOk searchOk = new SearchOk(result, server.self);
                    server.reply(searchOk, search);
                }
            }

            case SEROK -> {
                SearchOk searchOk = new SearchOk();
                searchOk.parseMessage(message);

                switch (searchOk.getState()) {
                    case SUCCESS -> {
                        // what to do print it?

                    }
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

        SER("SER", "search for file"),
        SEROK("SEROK", "file found");


        public final String id;
        public final String description;

        Token(String id, String description) {
            this.id = id;
            this.description = description;
        }
    }
}
