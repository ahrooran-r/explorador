package lk.uom.dc.log;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import lombok.NoArgsConstructor;
import org.slf4j.LoggerFactory;

import java.nio.file.Paths;

@NoArgsConstructor
public class LogbackInitializer {
    static {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        String path = Paths.get("config", "logback.xml").toAbsolutePath().toString();

        try {
            JoranConfigurator configurator = new JoranConfigurator();
            configurator.setContext(context);
            context.reset();
            configurator.doConfigure(path);
        } catch (JoranException ignored) {
        }
    }
}
