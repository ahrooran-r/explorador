package lk.uom.dc.log;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.sift.Discriminator;

public class LoggerNameBasedDiscriminator implements Discriminator<ILoggingEvent> {

    private static final String KEY = "fileName";

    private boolean started;

    @Override
    public String getDiscriminatingValue(ILoggingEvent event) {

        LogManager logger = LogManager.get(event.getLoggerName());

        if (null != logger) {
            String fileName = logger.getFile();
            return null != fileName ? fileName : "other";

        } else {
            return "other";
        }

    }

    @Override
    public String getKey() {
        return KEY;
    }

    @Override
    public void start() {
        started = true;
    }

    @Override
    public void stop() {
        started = false;
    }

    @Override
    public boolean isStarted() {
        return started;
    }
}
