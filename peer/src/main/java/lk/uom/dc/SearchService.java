package lk.uom.dc;

import lk.uom.dc.data.message.Search;
import lk.uom.dc.data.message.SearchOk;
import lk.uom.dc.settings.Settings;
import lombok.AllArgsConstructor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

import static lk.uom.dc.log.LogManager.APP;

/**
 * Helps with search operations
 */
@AllArgsConstructor
public class SearchService {

    private final PeerServer server;

    /**
     * Store the past queries here.
     */
    private static final Map<String /* word */, Map<Peer, LocalDateTime> /* peers that originally sent */> queryHistory = new HashMap<>();

    /**
     * A crude implementation of search corpus.
     */
    private static final Map<String, Set<String>> CORPUS;

    static {
        var temp = new HashMap<String, Set<String>>();

        // populate map
        try {
            APP.info("Populating corpus index");
            var lines = Files.readAllLines(Settings.NAMES_PATH, StandardCharsets.UTF_8);
            for (String line : lines) {
                var words = line.split(" ");
                for (String word : words) {
                    if (temp.containsKey(word)) {
                        temp.get(word).add(line);
                    } else {
                        Set<String> values = new HashSet<>();
                        values.add(line);
                        temp.put(word, values);
                    }
                }
            }
            APP.info("Populating corpus index DONE");
        } catch (IOException logIt) {
            APP.error("Unable to populate corpus", logIt);
            System.exit(-1);
        }

        CORPUS = Collections.unmodifiableMap(temp);
    }

    /**
     * @param word e.g.: baby, car etc. random chunks will not work -> like by, cr, bab etc.
     * @return either set of file names / null
     */
    public Set<String> search(String word) {
        var result = CORPUS.get(word);
        return null == result ? new HashSet<>() : result;
    }

    /**
     * Initiate a search request
     */
    public void searchNeighbours(String word) {
        Search searchMessage = new Search(server.self, word);
        try {
            server.send(searchMessage, server.first());
        } catch (IOException justLogIt) {
            APP.error("could not send search request to peer: {}", server.first().getUsername(), justLogIt);
        }

        try {
            server.send(searchMessage, server.second());
        } catch (IOException justLogIt) {
            APP.error("could not send search request to peer: {}", server.second().getUsername(), justLogIt);
        }

        // // store query in map
        // if (queryHistory.containsKey(word)) {
        //     queryHistory.get(word).add(server.self);
        // } else {
        //     queryHistory.put(word, new HashSet<>(Collections.singleton(server.self)));
        // }
    }

    /**
     * Pass search received from one peer to another peer.
     * This suffers from a cyclic search whereby
     * <ul>
     *     <li> A has peers B and C </li>
     *     <li> B has peers A and C </li>
     *     <li> C has peers A and B </li>
     * </ul>
     * <p>
     * Now if A sends search message and B gets it B will send to C. C will again send to A.
     * A dumb avoidance mechanism is to store the incoming requests and time and
     * if this node gets same request from same peer, then that will be discarded.
     */
    public void pass(Search search) {

        boolean newPeer = false;

        // store query in map
        var histories = queryHistory.get(search.query());
        if (null != histories) {
            LocalDateTime lastReceived = histories.get(search.sender());

            if (null != lastReceived) {
                if (Duration.between(lastReceived, LocalDateTime.now()).compareTo(Settings.HISTORY_PRESERVE_THRESHOLD) > 0) {
                    // pass message
                    pass0(search);

                } else {
                    // discard message
                    APP.info("discarding recent message: {}", search);
                }
            } else {
                if (pass0(search)) histories.put(search.sender(), LocalDateTime.now());
            }

        } else newPeer = true;

        if (newPeer) {
            if (pass0(search)) {
                histories = new HashMap<>();
                histories.put(search.sender(), LocalDateTime.now());
                queryHistory.put(search.query(), histories);
            }
        }
    }

    private boolean pass0(Search search) {
        boolean firstFailed = false;
        boolean secondFailed = false;

        try {
            // if origin of message is from first / second, I don't have to send it back to first / second
            if (!search.sender().fuzzyEquals(server.first())) server.send(search, server.first());
        } catch (IOException logIt) {
            firstFailed = true;
            APP.error("could not pass search requests to peer: {}", server.first(), logIt);
        }
        try {
            if (!search.sender().fuzzyEquals(server.second())) server.send(search, server.second());
        } catch (IOException logIt) {
            secondFailed = true;
            APP.error("could not pass search requests to peer: {}", server.second(), logIt);
        }

        if (firstFailed && secondFailed) {
            SearchOk error = new SearchOk(SearchOk.Token.ERROR, server.self);
            try {
                server.send(error, search.sender());
            } catch (IOException justLogIt) {
                APP.error("unable to pass on to peer: {}", search.sender(), justLogIt);
            }
            return false;
        }

        return true;
    }
}
