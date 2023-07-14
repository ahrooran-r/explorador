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
 *
 * <pre>length REG IP_address port_no username</pre>
 * <p>
 * e.g., 0036 REG 129.82.123.45 5001 1234abcd
 *
 * <ol>
 *     <li>
 *         length – Length of the entire username including 4 characters used to indicate the length.
 *         Always give length in xxxx format to make it easy to determine the length of the username.
 *     </li>
 *     <li>REG – Registration request.</li>
 *     <li>
 *         IP_address – IP address in xxx.xxx.xxx.xxx format.
 *         This is the IP address other nodes will use to reach you. Indicated with up to 15 characters.
 *     </li>
 *     <li>port_no – Port number. This is the port number that other nodes will connect to. Up to 5 characters.</li>
 *     <li>username – A string with characters and numbers.</li>
 * </ol>
 */
@NoArgsConstructor
@Getter
public class Request extends Message<Request.Token> {

    private Token type;

    public Request(Token type, Peer sender) {
        this.type = type;
        super.sender = sender;
    }

    @Override
    public void parseMessage(String[] message) {

        type = Token.valueOf(message[1].toUpperCase());

        final String host = message[2];
        final int port = Integer.parseInt(message[3]);
        sender = new Peer(new InetSocketAddress(host, port), Settings.UNKNOWN_USER);
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
        REG("register request"),
        UNREG("un register request"),
        ECHO("echo request");

        public final String description;

        Token(String description) {
            this.description = description;
        }
    }
}
