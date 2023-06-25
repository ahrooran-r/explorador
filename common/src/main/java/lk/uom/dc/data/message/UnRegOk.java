package lk.uom.dc.data.message;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.StringJoiner;

@Getter
@Setter(AccessLevel.NONE)
public class UnRegOk extends Message {

    @Getter(AccessLevel.NONE)
    private Token parent;

    private Token value;

    public UnRegOk(Token value) {
        this.parent = Token.UNROK;
        this.value = value;
    }

    @Override
    public void parseMessage(String raw) {
        // String[] split = raw.split(" ");
        //
        // int length = Integer.parseInt(split[0]);
        // if (length < 0 || length > 9999)
        //     throw new IllegalArgumentException("username length must be between 0 and 9999");
        //
        // String command = split[1];
        //
        // String host = split[2];
        // int port = Integer.parseInt(split[3]);
        // InetSocketAddress sender = new InetSocketAddress(host, port);
        //
        // String message = split[3];
        //
        // return new RegOk(length, command, sender, message);
    }

    @Override
    protected StringJoiner toStringJoiner() {
        return new StringJoiner(" ")
                .add(parent.name().toUpperCase())
                .add(value.id);
    }

    public enum Token {

        UNROK("UNROK", "unregister command"),
        NO_NODES("0", "successful"),
        PORT_OCCUPIED("9997", "error while unregistering. IP and port may not be in the registry or command is incorrect.");

        public final String id;
        public final String description;

        Token(String id, String description) {
            this.id = id;
            this.description = description;
        }
    }
}
