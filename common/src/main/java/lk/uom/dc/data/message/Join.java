package lk.uom.dc.data.message;

import lk.uom.dc.Peer;
import lk.uom.dc.settings.Settings;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.net.InetSocketAddress;
import java.util.*;

/**
 * Message would look something like this:
 * length -> sender of message -> sender's username
 *
 * <pre>length JOIN IP_address port_no </pre>
 * <p>
 * e.g., 0036 JOIN 129.82.123.45 5001
 */
@NoArgsConstructor
@Getter
public class Join extends Message<Join.Token> {

    private Token type;

    private Token state;

    public Join(Token type, Peer sender) {
        super.sender = sender;
        this.type = type;
    }

    @Override
    public void parseMessage(String[] message) {

        type = Token.find(message[1].toUpperCase());

        switch (type) {
            case JOIN, LEAVE -> {
                final String host = message[2];
                final int port = Integer.parseInt(message[3]);
                sender = new Peer(new InetSocketAddress(host, port), Settings.UNKNOWN_USER);
                state = null;
            }

            case JOINOK, LEAVEOK -> {
                String success = message[2];
                state = Token.find(success);
            }
        }

    }

    @Override
    protected StringJoiner toStringJoiner() {
        Objects.requireNonNull(type);
        Objects.requireNonNull(sender);

        switch (type) {
            case JOIN, LEAVE -> {
                return new StringJoiner(Settings.FS)
                        .add(type.name().toUpperCase())
                        .add(sender.getSocket().getAddress().getHostAddress())
                        .add(String.valueOf(sender.getSocket().getPort()));
            }

            case JOINOK, LEAVEOK -> {
                return new StringJoiner(Settings.FS)
                        .add(type.name().toUpperCase())
                        .add(state.id);
            }

            default -> throw new IllegalStateException("unable to construct a join message");
        }
    }

    public enum Token {

        JOIN("JOIN", "join with me"),
        LEAVE("LEAVE", "leave me"),

        JOINOK("JOINOK", "accept join invite"),
        LEAVEOK("LEAVEOK", "leave accepted"),
        SUCCESS("0", "join success"),
        ERROR("9999", "error while adding / reject join invite");

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
