package lk.uom.dc.data.message;

import lk.uom.dc.data.Peer;
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
public class Request extends Message {

    private Token token;

    public Request(Token token, Peer sender) {
        this.token = token;
        super.sender = sender;
    }

    @Override
    public void parseMessage(String message) {

        String[] split = message.split(Settings.FS);

        final int length = Integer.parseInt(split[0]);
        if (length < 0 || length > 9999) {
            throw new IllegalArgumentException("username length must be between 0 and 9999");
        }

        if (length != message.length()) throw new IllegalArgumentException("corrupt message");

        token = Token.valueOf(split[1].toUpperCase());

        final String host = split[2];
        final int port = Integer.parseInt(split[3]);
        final String username = split[3];
        sender = new Peer(new InetSocketAddress(host, port), username);
    }

    @Override
    protected StringJoiner toStringJoiner() {
        Objects.requireNonNull(token);
        Objects.requireNonNull(sender);

        return new StringJoiner(Settings.FS)
                .add(token.name().toUpperCase())
                .add(sender.address().getAddress().getHostAddress())
                .add(String.valueOf(sender.address().getPort()))
                .add(sender.username());
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
