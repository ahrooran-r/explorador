package lk.uom.dc.data.message;

import lk.uom.dc.Peer;
import lk.uom.dc.settings.Settings;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;
import java.util.StringJoiner;

@NoArgsConstructor
@Getter
@Setter(AccessLevel.NONE)
public class EchoOk extends Message {

    @Getter(AccessLevel.NONE)
    private Token type;

    private Token state;

    public EchoOk(Token state, Peer sender) {
        this.type = Token.ECHOK;
        this.state = state;
        this.sender = sender;
    }

    @Override
    public void parseMessage(String message) {
    }

    @Override
    protected StringJoiner toStringJoiner() {
        Objects.requireNonNull(type);
        Objects.requireNonNull(state);

        return new StringJoiner(Settings.FS)
                .add(type.name().toUpperCase())
                .add(state.id);
    }

    public enum Token {

        ECHOK("ECHOK", "echo ok command"),
        SUCCESSFUL("0", "request is successful");

        public final String id;
        public final String description;

        Token(String id, String description) {
            this.id = id;
            this.description = description;
        }
    }
}
