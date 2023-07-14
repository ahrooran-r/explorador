package lk.uom.dc.data.message;

import lk.uom.dc.Peer;
import lk.uom.dc.settings.Settings;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.stream.Collectors;

@NoArgsConstructor
@Getter
public class SearchOk extends Message<SearchOk.Token> {

    private Token state;

    private int hops;

    Set<String> availableFiles;

    public SearchOk(Peer sender, int hops, Set<String> availableFiles) {
        this.type = Token.SEROK;
        this.state = Token.SUCCESS;
        super.sender = sender;
        this.hops = hops;
        this.availableFiles = availableFiles;
    }

    public SearchOk(Peer sender, Token state) {
        this.type = Token.SEROK;
        this.state = state;
        super.sender = sender;
    }

    @Override
    public void parseMessage(String[] message) {
        type = Token.valueOf(message[1]);

        try {

            // This is the success path
            int numFiles = Integer.parseInt(message[2]);
            availableFiles = Arrays.stream(message).sequential()
                    .skip(6) // first 5 are taken -> rest are the file names
                    .limit(numFiles) // limit to the given number of files
                    .collect(Collectors.toSet());
            state = Token.SUCCESS;

        } catch (NumberFormatException notSuccess) {
            state = Token.find(message[2]);
        }

        final String host = message[3];
        final int port = Integer.parseInt(message[4]);
        sender = new Peer(new InetSocketAddress(host, port), Settings.UNKNOWN_USER);

        hops = Integer.parseInt(message[5]);
    }

    @Override
    protected StringJoiner toStringJoiner() {
        Objects.requireNonNull(type);
        Objects.requireNonNull(sender);

        StringJoiner joiner = new StringJoiner(Settings.FS);
        switch (state) {
            case ERROR, NO_MATCH_FOUND, NODE_UNREACHABLE -> joiner.add(state.id);
            case SUCCESS -> {
                joiner.add(type.name().toUpperCase())
                        .add(String.valueOf(availableFiles.size()))
                        .add(sender.getSocket().getAddress().getHostAddress())
                        .add(String.valueOf(sender.getSocket().getPort()))
                        .add(String.valueOf(hops));
                for (String file : availableFiles) joiner.add(file);
            }
        }

        return joiner;
    }

    public enum Token {

        SEROK("SEROK", "file found"),

        SUCCESS("SUCCESS", "num of files >= 1"),
        NO_MATCH_FOUND("0", "no matching results. Searched key is not in key table"),
        NODE_UNREACHABLE("9999", "failure due to node unreachable"),
        ERROR("9998", "Generic error message, to indicate that a given command is not understood. For storing and searching files/keys this should be send to the initiator of the message.");

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
