package lk.uom.dc.data.message;

import lk.uom.dc.data.Peer;
import lk.uom.dc.settings.Settings;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;
import java.util.StringJoiner;

import static lk.uom.dc.log.LogManager.APP;

@NoArgsConstructor
@Getter
@Setter(AccessLevel.NONE)
public class UnRegOk extends Message {

    @Getter(AccessLevel.NONE)
    private Token parent;

    private Token value;

    public UnRegOk(Token value, Peer sender) {
        this.parent = Token.UNROK;
        this.value = value;
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

        parent = Token.valueOf(split[1].toUpperCase());
        value = Token.valueOf(split[2].toUpperCase());

        switch (value) {
            case SUCCESS -> APP.info("{}, message: {}", value.description, message);
            case FAILURE -> APP.error("{}, message: {}", value.description, message);
        }

        // sender is not set yet
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

        UNROK("UNROK", "unregister command"),
        SUCCESS("0", "successfully unregistered"),
        FAILURE("9997", "error while unregistering. IP and port may not be in the registry or command is incorrect.");

        public final String id;
        public final String description;

        Token(String id, String description) {
            this.id = id;
            this.description = description;
        }
    }
}
