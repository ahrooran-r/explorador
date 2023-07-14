package lk.uom.dc;

import lk.uom.dc.data.message.Search;
import lk.uom.dc.data.message.SearchOk;
import lk.uom.dc.settings.Settings;
import lombok.AllArgsConstructor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

import static lk.uom.dc.log.LogManager.APP;

/**
 * Helps with search operations
 */
@AllArgsConstructor
public class SearchService {

    private final PeerServer peerServer;

    /**
     * A crude implementation of search corpus.
     */
    private static final Map<String, Set<String>> corpus;

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

        corpus = Collections.unmodifiableMap(temp);
    }

    /**
     * @param word e.g.: baby, car etc. random chunks will not work -> like by, cr, bab etc.
     * @return either set of file names / null
     */
    public Set<String> search(String word) {
        var result = corpus.get(word);
        return null == result ? new HashSet<>() : result;
    }

    /**
     * Initiate a search request
     */
    public void searchNeighbours(String fileName) {
        Search searchMessage = new Search(peerServer.self, fileName);
        try {
            peerServer.send(searchMessage, peerServer.getFirst());
        } catch (IOException justLogIt) {
            APP.error("could not send search request to peer: {}", peerServer.getFirst().getUsername(), justLogIt);
        }

        try {
            peerServer.send(searchMessage, peerServer.getSecond());
        } catch (IOException justLogIt) {
            APP.error("could not send search request to peer: {}", peerServer.getSecond().getUsername(), justLogIt);
        }
    }

    /**
     * Pass search received from one peer to another peer
     */
    public void pass(Search search, Peer from) {
        Peer to = from.equals(peerServer.getFirst()) ? peerServer.getSecond() : peerServer.getFirst();
        try {
            peerServer.send(search, to);
        } catch (IOException e) {
            APP.error("could not send search request to peer: {}", to, e);
            SearchOk error = new SearchOk(peerServer.self, SearchOk.Token.ERROR);
            try {
                peerServer.send(error, to);
            } catch (IOException justLogIt) {
                APP.error("unable to pass on to peer: {}", to, justLogIt);
            }
        }
    }
}
