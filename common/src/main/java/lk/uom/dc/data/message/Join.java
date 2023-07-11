package lk.uom.dc.data.message;

import lk.uom.dc.Peer;
import lk.uom.dc.settings.Settings;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.net.InetSocketAddress;
import java.util.Objects;
import java.util.StringJoiner;

/**
 * Message would look something like this:
 * length -> sender of message -> sender's username
 *
 * <pre>length JOIN IP_address port_no username num_hops </pre>
 * <p>
 * e.g., 0036 JOIN 129.82.123.45 5001 1234abcd
 */
@NoArgsConstructor
@Getter
public class Join extends Message {

    private Token type;

    public Join(Token type, Peer sender) {
        super.sender = sender;
        this.type = type;
    }

    @Override
    public void parseMessage(String message) {

        String[] split = message.split(Settings.FS);

        final int length = Integer.parseInt(split[0]);
        if (length < 0 || length > 9999) {
            throw new IllegalArgumentException("username length must be between 0 and 9999");
        }

        if (length != message.length()) throw new IllegalArgumentException("corrupt message");

        type = Token.valueOf(split[1].toUpperCase());

        final String host = split[2];
        final int port = Integer.parseInt(split[3]);
        final String username = split[4];
        sender = new Peer(new InetSocketAddress(host, port), username);
    }

    @Override
    protected StringJoiner toStringJoiner() {
        Objects.requireNonNull(type);
        Objects.requireNonNull(sender);

        return new StringJoiner(Settings.FS)
                .add(type.name().toUpperCase())
                .add(sender.getSocket().getAddress().getHostAddress())
                .add(String.valueOf(sender.getSocket().getPort()))
                .add(sender.getUsername());
    }

    public enum Token {

        JOIN("JOIN", "join with me"),
        JOINOK("JOINOK", "accept join invite"),
        NOJOIN("NOJOIN", "reject join invite"),

        UNJOIN("UNJOIN", "unjoin with me");
        // this does not have an ok message -> fire and forget

        public final String id;
        public final String description;

        Token(String id, String description) {
            this.id = id;
            this.description = description;
        }
    }
}
