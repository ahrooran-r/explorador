package lk.uom.dc.log;

import ch.qos.logback.classic.pattern.ClassOfCallerConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;

public class DelegateAwareClassCallerConverter extends ClassOfCallerConverter {
    @Override
    protected String getFullyQualifiedName(ILoggingEvent event) {

        StackTraceElement[] cda = event.getCallerData();
        if (cda != null && cda.length > 1) {
            return cda[1].getClassName();
        } else {
            return super.getFullyQualifiedName(event);
        }
    }
}
