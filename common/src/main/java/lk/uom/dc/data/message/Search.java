package lk.uom.dc.data.message;

import lk.uom.dc.Peer;
import lk.uom.dc.settings.Settings;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.net.InetSocketAddress;
import java.util.Objects;
import java.util.StringJoiner;

@NoArgsConstructor
public class Search extends Message<Search.Token> {

    private String query;

    @Setter
    private int hops;

    public Search(Peer sender, String query) {
        this.type = Token.SER;
        this.query = query;
        this.hops = 0;
        super.sender = sender;
    }

    @Override
    public void parseMessage(String[] message) {
        type = Token.valueOf(message[1]);

        final String host = message[2];
        final int port = Integer.parseInt(message[3]);
        sender = new Peer(new InetSocketAddress(host, port), Settings.UNKNOWN_USER);

        this.query = message[4]
                .replaceAll("\"", "")
                .replaceAll(Settings.IS, " ");

        try {
            this.hops = Integer.parseInt(message[5]);
        } catch (RuntimeException ignored) {
            this.hops = 0;
        }
    }

    @Override
    protected StringJoiner toStringJoiner() {
        Objects.requireNonNull(type);
        Objects.requireNonNull(sender);

        return new StringJoiner(Settings.FS)
                .add(type.name().toUpperCase())
                .add(sender.getSocket().getAddress().getHostAddress())
                .add(String.valueOf(sender.getSocket().getPort()))
                .add("\"" + query.replaceAll(" ", Settings.IS) + "\"")
                .add(hops == 0 ? "" : String.valueOf(hops));
    }

    public enum Token {

        SER("SER", "search for file");

        public final String id;
        public final String description;

        Token(String id, String description) {
            this.id = id;
            this.description = description;
        }
    }

    public String query() {
        return query;
    }

    public int hops() {
        return this.hops;
    }
}
