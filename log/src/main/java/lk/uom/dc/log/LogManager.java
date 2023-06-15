package lk.uom.dc.log;

import lk.uom.dc.settings.Base;
import lk.uom.dc.settings.Constants;
import lk.uom.dc.settings.LogConfig;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Delegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

import java.io.IOException;

@Getter
public enum LogManager {

    APP("app"),
    IN("in"),
    OUT("out"),
    DB("db"),
    KAFKA("kafka"),
    SUMMARY("summary");

    @Delegate
    @Getter(AccessLevel.NONE)
    private Logger logger;

    private String file;

    private Level level;

    private boolean enabled;

    private boolean enableConsole;

    static {
        new LogbackInitializer();
    }

    LogManager(String name) {

        try {
            this.file = LogConfig.get(name, "file").asText();
            this.enabled = LogConfig.get(name, "enabled").asBoolean();
            this.enableConsole = LogConfig.get(name, "console").asBoolean();
            this.level = Base.getMapper().treeToValue(LogConfig.get(name, "level"), Level.class);
            this.logger = LoggerFactory.getLogger(Constants.PACKAGE_NAME + "." + name);

        } catch (IOException ioException) {
            ioException.printStackTrace();
            System.exit(1039);
        }
    }

    public static LogManager get(String fullyQualifiedName) {
        final String prefix = Constants.PACKAGE_NAME + ".";
        return switch (fullyQualifiedName.toLowerCase()) {
            case prefix + "app" -> LogManager.APP;
            case prefix + "in" -> LogManager.IN;
            case prefix + "out" -> LogManager.OUT;
            case prefix + "db" -> LogManager.DB;
            case prefix + "kafka" -> LogManager.KAFKA;
            case prefix + "summary" -> LogManager.SUMMARY;
            default -> null;
        };
    }
}
