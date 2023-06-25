package lk.uom.dc.data.message;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.StringJoiner;

@Getter
@Setter(AccessLevel.NONE)
public class EchoOk extends Message {

    @Getter(AccessLevel.NONE)
    private Token parent;

    private Token value;

    public EchoOk(Token value) {
        this.parent = Token.ECHOK;
        this.value = value;
    }

    @Override
    public void parseMessage(String raw) {
    }

    @Override
    protected StringJoiner toStringJoiner() {
        StringJoiner joiner = new StringJoiner(" ")
                .add(parent.name().toUpperCase())
                .add(value.id);

        return joiner;
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
