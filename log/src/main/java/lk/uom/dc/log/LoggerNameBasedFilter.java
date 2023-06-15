package lk.uom.dc.log;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;

public class LoggerNameBasedFilter extends Filter<ILoggingEvent> {

    @Override
    public FilterReply decide(ILoggingEvent event) {

        LogManager logger = LogManager.get(event.getLoggerName());
        if (null != logger) {

            boolean isLevelSatisfied = event.getLevel().isGreaterOrEqual(Level.toLevel(logger.getLevel().name()));

            if (logger.isEnabled() || logger.isEnableConsole()) return isLevelSatisfied ? FilterReply.ACCEPT : FilterReply.DENY;
            else return FilterReply.DENY;

        } else {
            return FilterReply.ACCEPT;
        }
    }
}
