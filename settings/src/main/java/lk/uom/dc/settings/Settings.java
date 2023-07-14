package lk.uom.dc.settings;

import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;

public class Settings {

    public static final String PACKAGE_NAME = "lk.uom.dc";

    /**
     * Field separator
     */
    public final static String FS = " ";
    /**
     * Index separator. For file names. Since " " is already used,
     * sharing file names in such a way would not be trivial.
     * File name " " chars will be re-encoded with "_" when sending and decoded when receiving.
     */
    public final static String IS = "_";

    public static final Path LOG_SETTINGS_PATH = Paths.get("config", "log-settings.yml");
    public static final Path APP_SETTINGS_PATH = Paths.get("config", "application.yml");
    public static final Path NAMES_PATH = Paths.get("config", "names.txt");
    public static final Path QUERIES_PATH = Paths.get("config", "queries.txt");

    /**
     * Peer server settings
     */
    public static final String PEER_HOST;
    public static final int PEER_PORT;
    public static final String PEER_USERNAME;
    public static final Duration PING_INTERVAL;
    public static final int NUM_PEERS;
    public static final int FAILURE_COUNT;
    public static final String UNKNOWN_USER = "unknown";

    /**
     * Bootstrap server settings
     * */
    public static final String BOOTSTRAP_HOST;
    public static final String BOOTSTRAP_USERNAME = "bootstrap";
    public static final int BOOTSTRAP_PORT;

    /**
     * can be used for all server messages
     */
    public static final int BOOTSTRAP_MSG_SIZE;

    private static final JsonNode application;

    static {
        try {
            application = Yaml
                    .getMapper()
                    .readTree(Files.newBufferedReader(Settings.APP_SETTINGS_PATH, StandardCharsets.UTF_8));
        } catch (IOException rethrow) {
            System.exit(-1);
            throw new RuntimeException(rethrow);
        }

        final JsonNode bootstrap = application.get("bootstrap");
        BOOTSTRAP_HOST = bootstrap.get("host").asText();
        BOOTSTRAP_PORT = bootstrap.get("port").asInt();
        BOOTSTRAP_MSG_SIZE = bootstrap.get("msgSize").intValue();


        final JsonNode peerServer = application.get("peerServer");
        PEER_HOST = peerServer.get("host").asText();
        PEER_PORT = peerServer.get("port").asInt();
        PEER_USERNAME = peerServer.get("username").asText();
        PING_INTERVAL = Duration.parse(peerServer.get("pingInterval").asText());
        NUM_PEERS = peerServer.get("numPeers").intValue();
        FAILURE_COUNT = peerServer.get("failureCount").intValue();
    }

    private Settings() {
    }
}
