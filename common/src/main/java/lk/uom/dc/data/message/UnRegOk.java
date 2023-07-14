package lk.uom.dc.data.message;

import lk.uom.dc.Peer;
import lk.uom.dc.settings.Settings;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.*;

import static lk.uom.dc.log.LogManager.APP;

@NoArgsConstructor
@Getter
@Setter(AccessLevel.NONE)
public class UnRegOk extends Message<UnRegOk.Token> {

    @Getter(AccessLevel.NONE)
    private Token type;

    private Token state;

    public UnRegOk(Token state, Peer sender) {
        this.type = Token.UNROK;
        this.state = state;
        this.sender = sender;
    }

    @Override
    public void parseMessage(String[] message) {
        type = Token.find(message[1]);
        state = Token.find(message[2]);

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

        private static final Map<String, Token> inversionMap;

        static {
            Map<String, Token> invert = HashMap.newHashMap(6);
            Arrays.stream(values()).sequential().forEach(token -> invert.put(token.id, token));
            inversionMap = Collections.unmodifiableMap(invert);
        }

        public static Token find(String key) {
            return inversionMap.get(key);
        }

        Token(String id, String description) {
            this.id = id;
            this.description = description;
        }
    }
}
