package lk.uom.dc.data.message;

import lk.uom.dc.data.Peer;
import lk.uom.dc.settings.Settings;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;
import java.util.StringJoiner;

import static lk.uom.dc.log.LogManager.PING;

@NoArgsConstructor
@Getter
@Setter(AccessLevel.NONE)
public class PingPong extends Message {

    @Getter(AccessLevel.NONE)
    private Token token;

    public PingPong(Peer sender) {
        this.sender = sender;
    }

    @Override
    public void parseMessage(String message) {
        String[] split = message.split(Settings.FS);

        final int length = Integer.parseInt(split[0]);
        if (length < 0 || length > 9999) {
            throw new IllegalArgumentException("length must be between 0 and 9999");
        }

        if (length != message.length()) throw new IllegalArgumentException("corrupt message");

        token = Token.valueOf(split[1].toUpperCase());

        String host = split[2];
        int port = Integer.parseInt(split[3]);

        switch (token) {
            case PING, PONG -> PING.info("{}, from -> host: {}, port: {}", token.description, host, port);
        }
    }

    @Override
    protected StringJoiner toStringJoiner() {
        Objects.requireNonNull(token);
        Objects.requireNonNull(sender);

        return new StringJoiner(Settings.FS)
                .add(token.name().toUpperCase())
                .add(sender.address().getAddress().getHostAddress())
                .add(String.valueOf(sender.address().getPort()));
    }

    public enum Token {

        PING("PING", "calling ping"),
        PONG("PONG", "echoing pong");

        public final String id;
        public final String description;

        Token(String id, String description) {
            this.id = id;
            this.description = description;
        }
    }
}
