package lk.uom.dc.data.message;

import lk.uom.dc.data.Peer;
import lk.uom.dc.settings.Settings;
import lombok.Getter;

import java.net.DatagramPacket;
import java.nio.charset.StandardCharsets;
import java.util.StringJoiner;

import static lk.uom.dc.log.LogManager.IN;

public abstract class Message {

    @Getter
    protected Peer sender;

    protected abstract void parseMessage(String message);

    protected abstract StringJoiner toStringJoiner();

    public void parseMessage(DatagramPacket message) {
        String raw = new String(message.getData(), 0, message.getLength(), StandardCharsets.UTF_8);
        IN.info("{} : {} - {}", message.getAddress().getHostAddress(), message.getPort(), raw);
        parseMessage(raw);
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
