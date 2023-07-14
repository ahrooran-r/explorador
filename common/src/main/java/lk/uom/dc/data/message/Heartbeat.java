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
public class Heartbeat extends Message<Heartbeat.Token> {

    @Getter(AccessLevel.NONE)
    private Token state;

    public Heartbeat(Token state, Peer from) {
        this.state = state;
        this.sender = from;
    }

    @Override
    public void parseMessage(String[] message) {
        state = Token.valueOf(message[1].toUpperCase());

        switch (state) {
            case PING, PONG -> PING.info("{}, from -> host: {}, port: {}",
                    state.description,
                    sender.getSocket().getAddress(),
                    sender.getSocket().getPort()
            );
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
