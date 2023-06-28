package lk.uom.dc;

import lk.uom.dc.data.Peer;
import lk.uom.dc.data.message.EchoOk;
import lk.uom.dc.data.message.RegOk;
import lk.uom.dc.data.message.UnRegOk;
import lk.uom.dc.settings.Settings;
import lombok.SneakyThrows;

import java.net.DatagramPacket;
import java.net.InetSocketAddress;

public class PeerListener implements PeerMessageListener, Runnable {

    private final PeerServer peerServer;

    public PeerListener(PeerServer peerServer) {
        this.peerServer = peerServer;
    }

    @Override
    public void onMessage(String message, InetSocketAddress sender) {
        // length -> 0, token -> 1
        Token token = Token.valueOf(message.split(Settings.FS)[1].toUpperCase());

        switch (token) {

            case REGOK -> {
                RegOk regOk = new RegOk(new Peer(sender, "bootstrap"));
                regOk.parseMessage(message);

                if (null == peerServer.getFirst()) peerServer.setFirst(regOk.getFirst());
                if (null == peerServer.getSecond()) peerServer.setSecond(regOk.getSecond());
            }

            case UNROK -> {
                UnRegOk unRegOk = new UnRegOk();
                unRegOk.parseMessage(message);

                // nothing to do at this point -> simply shutdown
                System.exit(-1);
            }

            case ECHOK -> {
                EchoOk echoOk = new EchoOk();
                echoOk.parseMessage(message);
            }
        }
    }

    @SneakyThrows
    @Override
    public void run() {
        //noinspection InfiniteLoopStatement
        while (true) {
            byte[] buffer = new byte[Settings.BOOTSTRAP_MSG_SIZE];
            DatagramPacket incoming = new DatagramPacket(buffer, buffer.length);
            peerServer.getPeerSocket().receive(incoming);
            onMessage(incoming);
        }

    }

    enum Token {

        REGOK("REGOK", "register command"),
        UNROK("UNROK", "unregister command"),
        ECHOK("ECHOK", "echo command");

        public final String id;
        public final String description;

        Token(String id, String description) {
            this.id = id;
            this.description = description;
        }
    }
}
