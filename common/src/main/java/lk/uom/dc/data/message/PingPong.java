package lk.uom.dc.data.message;

import lk.uom.dc.Peer;
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
    private Token state;

    public PingPong(Token state, Peer from) {
        this.state = state;
        this.sender = from;
    }

    @Override
    public void parseMessage(String message) {
        String[] split = message.split(Settings.FS);

        final int length = Integer.parseInt(split[0]);
        if (length < 0 || length > 9999) {
            throw new IllegalArgumentException("length must be between 0 and 9999");
        }

        if (length != message.length()) throw new IllegalArgumentException("corrupt message");

        state = Token.valueOf(split[1].toUpperCase());

        String host = split[2];
        int port = Integer.parseInt(split[3]);

        switch (state) {
            case PING, PONG -> PING.info("{}, from -> host: {}, port: {}", state.description, host, port);
        }
    }

    @Override
    protected StringJoiner toStringJoiner() {
        Objects.requireNonNull(state);
        Objects.requireNonNull(sender);

        return new StringJoiner(Settings.FS)
                .add(state.name().toUpperCase())
                .add(sender.getSocket().getAddress().getHostAddress())
                .add(String.valueOf(sender.getSocket().getPort()));
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
