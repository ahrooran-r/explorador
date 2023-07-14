package lk.uom.dc.data.message;

import lk.uom.dc.Peer;
import lk.uom.dc.settings.Settings;
import lombok.AccessLevel;
import lombok.Getter;

import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.StringJoiner;

import static lk.uom.dc.log.LogManager.IN;

public abstract class Message<T> {

    // message type
    @Getter(AccessLevel.NONE)
    protected T type;

    @Getter
    protected Peer sender;

    protected abstract void parseMessage(String[] message);

    protected abstract StringJoiner toStringJoiner();

    public void parseMessage(DatagramPacket message) {
        String raw = new String(message.getData(), 0, message.getLength(), StandardCharsets.UTF_8);
        IN.info("{} : {} - {}", message.getAddress().getHostAddress(), message.getPort(), raw);

        sender = new Peer((InetSocketAddress) message.getSocketAddress(), Settings.UNKNOWN_USER);
        String[] chunks = sanitize(raw);
        parseMessage(chunks);
    }

    /**
     * Performs initial checks and breaks message into chunks
     *
     * @param message raw string message
     */
    private String[] sanitize(String message) {

        String[] chunks = message.split(Settings.FS);

        final int length = Integer.parseInt(chunks[0]);
        if (length < 0 || length > 9999) {
            throw new IllegalArgumentException("username length must be between 0 and 9999");
        }

        if (length != message.length()) throw new IllegalArgumentException("corrupt message");
        return chunks;
    }

    @Override
    public String toString() {
        StringJoiner joiner = toStringJoiner();
        int length = joiner.length();

        // 4 for length size and 1 for space
        return new StringJoiner(Settings.FS)
                .add(String.format("%04d", length + 5))
                .merge(joiner)
                .toString();
    }

}
