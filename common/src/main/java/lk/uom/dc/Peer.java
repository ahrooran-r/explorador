package lk.uom.dc;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.net.InetSocketAddress;
import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@ToString
public class Peer {

    /**
     * marks that this peer is connected to current peer server
     */
    @Setter
    private volatile transient boolean connected;

    /**
     * The last pinged time for this peer.
     */
    @Setter
    private transient LocalDateTime lastPinged;

    private final InetSocketAddress socket;
    private final String username;

    public Peer(InetSocketAddress socket, String username) {
        Objects.requireNonNull(socket);
        if (null == username || 0 == username.length()) throw new IllegalArgumentException("username is empty");
        this.socket = socket;
        this.username = username;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Peer peer = (Peer) o;

        if (socket.getPort() != peer.socket.getPort()) return false;
        return username.equals(peer.username);
    }

    @Override
    public int hashCode() {
        return 31 * socket.getPort() + username.hashCode();
    }
}
