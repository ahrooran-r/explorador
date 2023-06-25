package lk.uom.dc.data.message;

import java.util.StringJoiner;

public abstract class Message {

    protected final static String DELIMITER = " ";

    public abstract void parseMessage(String raw);

    protected abstract StringJoiner toStringJoiner();

    @Override
    public String toString() {
        StringJoiner joiner = toStringJoiner();
        int length = joiner.length();

        // 4 for length size and 1 for space
        return new StringJoiner(" ")
                .add(String.format("%04d", length + 5))
                .merge(joiner)
                .toString();
    }

}
