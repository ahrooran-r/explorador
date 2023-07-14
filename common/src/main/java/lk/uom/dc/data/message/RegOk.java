package lk.uom.dc.data.message;

import lk.uom.dc.Peer;
import lk.uom.dc.settings.Settings;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.net.InetSocketAddress;
import java.util.*;

import static lk.uom.dc.log.LogManager.APP;

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
@NoArgsConstructor
@Getter
@Setter(AccessLevel.NONE)
public class RegOk extends Message<RegOk.Token> {

    @Getter(AccessLevel.NONE)
    private Token type;

    private Token state;

    private Peer first;

    private Peer second;

    public RegOk(Peer sender) {
        super.sender = sender;
    }

    public RegOk(Token state, Peer sender) {
        this.type = Token.REGOK;
        this.state = state;
        super.sender = sender;
    }

    public RegOk(Peer first, Peer second, Peer sender) {
        this(null, sender);

        if (null == first) this.state = Token.NO_NODES;
        else {
            this.first = first;
            this.state = Token.ONE;

            if (null != second) {
                this.state = Token.TWO;
                this.second = second;
            }
        }
    }

    @Override
    public void parseMessage(String[] message) {

        type = Token.find(message[1].toUpperCase());

        state = Token.find(message[2]);

        String host;
        int port;
        switch (state) {
            case NO_NODES -> {
                APP.debug("No peers found");
                first = null;
                second = null;
            }

            case ONE -> {
                APP.debug("1 peer found");
                host = message[3];
                port = Integer.parseInt(message[3 + 1]);
                first = new Peer(new InetSocketAddress(host, port), UUID.randomUUID().toString());
                second = null;
            }

            case ERRONEOUS -> APP.error("{}}: {}, sender: ", state.description, message, sender);

            case ALREADY_REGISTERED, PORT_OCCUPIED, BS_FULL -> APP.error("{}, sender: ", state.description, sender);

            default -> {
                try {
                    final int peerCount = Integer.parseInt(message[2]);
                    final List<Peer> peers = new ArrayList<>(2);

                    // bootstrap can send more than 2 peers
                    for (int i = 0; i < peerCount; i += 2) {
                        host = message[3 + i];
                        port = Integer.parseInt(message[3 + 1 + i]);
                        Peer peer = new Peer(new InetSocketAddress(host, port), UUID.randomUUID().toString());

                        // I trust that bootstrap only sends unique peers
                        peers.add(peer);
                    }

                    Collections.shuffle(peers);
                    first = peers.get(0);
                    second = peers.get(1);


                } catch (IndexOutOfBoundsException ignored) {
                } catch (RuntimeException other) {
                    first = null;
                    second = null;
                }
            }
        }

        // sender is not set yet
    }

    @Override
    protected StringJoiner toStringJoiner() {
        Objects.requireNonNull(type);
        Objects.requireNonNull(state);

        StringJoiner joiner = new StringJoiner(Settings.FS)
                .add(type.name().toUpperCase())
                .add(state.id);

        if (null != first) {
            joiner.add(first.getSocket().getAddress().getHostAddress())
                    .add(String.valueOf(first.getSocket().getPort()));
        }

        if (null != second) {
            joiner.add(second.getSocket().getAddress().getHostAddress())
                    .add(String.valueOf(second.getSocket().getPort()));
        }

        return joiner;
    }

    public enum Token {
        REGOK("REGOK", "register ok command"),
        NO_NODES("0", "request is successful, no nodes in the system"),
        ONE("1", "request is successful, 1 node's contacts will be returned"),
        TWO("2", "request is successful, 1 or 2 nodes' contacts will be returned"),
        ALREADY_REGISTERED("9998", "failed, already registered to you, unregister first"),
        PORT_OCCUPIED("9997", "failed, registered to another user, try a different IP and port"),
        ERRONEOUS("9999", "failed, malformed message"),
        BS_FULL("9996", "failed, can’t register. BS full");

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
