package lk.uom.dc.log;

import lk.uom.dc.settings.Log;
import lk.uom.dc.settings.Settings;
import lk.uom.dc.settings.Yaml;
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
    PING("ping");

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
            this.file = Log.get(name, "file").asText();
            this.enabled = Log.get(name, "enabled").asBoolean();
            this.enableConsole = Log.get(name, "console").asBoolean();
            this.level = Yaml.getMapper().treeToValue(Log.get(name, "level"), Level.class);
            this.logger = LoggerFactory.getLogger(Settings.PACKAGE_NAME + "." + name);

        } catch (IOException ioException) {
            ioException.printStackTrace();
            System.exit(1039);
        }
    }

    public static LogManager get(String fullyQualifiedName) {
        final String prefix = Settings.PACKAGE_NAME + ".";
        return switch (fullyQualifiedName.toLowerCase()) {
            case prefix + "app" -> LogManager.APP;
            case prefix + "in" -> LogManager.IN;
            case prefix + "out" -> LogManager.OUT;
            case prefix + "ping" -> LogManager.PING;
            default -> null;
        };
    }
}
