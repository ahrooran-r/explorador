package lk.uom.dc.data.message;

import lk.uom.dc.data.Peer;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.StringJoiner;

/**
 * Register Response message – BS will send the following message
 * <p>
 * <pre>length REGOK no_nodes IP_1 port_1 IP_2 port_2</pre>
 * <p>
 * e.g., <pre>0051 REGOK 2 129.82.123.45 5001 64.12.123.190 34001</pre>
 *
 * <ol>
 *     <li>length – Length of the entire message including 4 characters used to indicate the length. In xxxx format.</li>
 *     <li>REGOK – Registration response.</li>
 *     <li>no_ nodes – Number of node entries that are going to be returned by the registry</li>
 *     <ol>
 *         <li>0 – request is successful, no nodes in the system</li>
 *         <li>1 or 2 – request is successful, 1 or 2 nodes' contacts will be returned</li>
 *         <li>9999 – failed, there is some error in the command</li>
 *         <li>9998 – failed, already registered to you, unregister first</li>
 *         <li>9997 – failed, registered to another user, try a different IP and port</li>
 *         <li>9996 – failed, can’t register. BS full.</li>
 *     </ol>
 *     <li>IP_1 – IP address of the 1st node (if available).</li>
 *     <li>port_1 – Port number of the 1st node (if available).</li>
 *     <li>IP_2 – IP address of the 2nd node (if available).</li>
 *     <li>port_2 – Port number of the 2nd node (if available).</li>
 * </ol>
 */
@Getter
@Setter(AccessLevel.NONE)
public class RegOk extends Message {

    @Getter(AccessLevel.NONE)
    private Token parent;

    private Token child;

    private Peer first;

    private Peer second;

    public RegOk(Token child) {
        this.parent = Token.REGOK;
        this.child = child;
    }

    public RegOk(Peer first, Peer second) {
        this(null);

        if (null == first) this.child = Token.NO_NODES;
        else {
            this.first = first;
            this.child = Token.ONE;

            if (null != second) {
                this.child = Token.TWO;
                this.second = second;
            }
        }
    }

    @Override
    public void parseMessage(String raw) {
    }

    @Override
    protected StringJoiner toStringJoiner() {
        StringJoiner joiner = new StringJoiner(" ")
                .add(parent.name().toUpperCase())
                .add(child.id);

        if (null != first) {
            joiner.add(first.socket().getAddress().getHostAddress())
                    .add(String.valueOf(first.socket().getPort()));
        }

        if (null != second) {
            joiner.add(second.socket().getAddress().getHostAddress())
                    .add(String.valueOf(second.socket().getPort()));
        }

        return joiner;
    }

    public enum Token {

        REGOK("REGOK", "register ok command"),
        NO_NODES("0", "request is successful, no nodes in the system"),
        ONE("1", "request is successful, 1 node's contacts will be returned"),
        TWO("2", "request is successful, 1 or 2 nodes' contacts will be returned"),
        ALREADY_REGISTERED("9998", "already registered to you, unregister first"),
        PORT_OCCUPIED("9997", "failed, registered to another user, try a different IP and port");

        public final String id;
        public final String description;

        Token(String id, String description) {
            this.id = id;
            this.description = description;
        }
    }
}
