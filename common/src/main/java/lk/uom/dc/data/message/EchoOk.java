package lk.uom.dc.data.message;

import lk.uom.dc.data.Peer;
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
    private Token parent;

    private Token value;

    public EchoOk(Token value, Peer sender) {
        this.parent = Token.ECHOK;
        this.value = value;
        this.sender = sender;
    }

    @Override
    public void parseMessage(String message) {
    }

    @Override
    protected StringJoiner toStringJoiner() {
        Objects.requireNonNull(parent);
        Objects.requireNonNull(value);

        return new StringJoiner(Settings.FS)
                .add(parent.name().toUpperCase())
                .add(value.id);
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
