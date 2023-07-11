package lk.uom.dc.data.message;

import lk.uom.dc.Peer;
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
    private Token type;

    private Token state;

    public UnRegOk(Token state, Peer sender) {
        this.type = Token.UNROK;
        this.state = state;
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

        type = Token.valueOf(split[1].toUpperCase());
        state = Token.valueOf(split[2].toUpperCase());

        switch (state) {
            case SUCCESS -> APP.info("{}, message: {}", state.description, message);
            case FAILURE -> APP.error("{}, message: {}", state.description, message);
        }

        // sender is not set yet
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
