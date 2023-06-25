package lk.uom.dc.data;

import java.net.InetSocketAddress;
import java.util.Objects;

public record Peer(
        InetSocketAddress socket,
        String username
) {

    public Peer {
        Objects.requireNonNull(socket);
        if (null == username || 0 == username.length()) throw new IllegalArgumentException("username is empty");
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
