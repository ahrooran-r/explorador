package lk.uom.dc.settings;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class LogConfig {

    private static final JsonNode logConfig;

    static {
        try {
            logConfig = Base
                    .getMapper()
                    .readTree(Files.newBufferedReader(Constants.LOG_SETTINGS_PATH, StandardCharsets.UTF_8));
        } catch (IOException rethrow) {
            throw new RuntimeException(rethrow);
        }
    }

    public static JsonNode get(String logger, String setting) {
        return logConfig.get(logger).get(setting);
    }
}
