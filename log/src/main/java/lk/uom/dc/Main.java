package lk.uom.dc;

import static lk.uom.dc.log.LogManager.*;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

/**
 * Please visit: my <a href="https://ahrooran.hashnode.dev/create-a-multi-logger-system-with-logback">hashnode article</a>
 * for detailed documentation and explanation
 */
@Slf4j
public class Main {

    @SneakyThrows
    public static void main(String[] args) {

        APP.trace("APP_TRACE");
        APP.debug("APP_DEBUG");
        APP.info("APP_INFO");
        APP.warn("APP_WARN");
        APP.error("APP_ERROR");

        IN.trace("IN_TRACE");
        IN.debug("IN_DEBUG");
        IN.info("IN_INFO");
        IN.warn("IN_WARN");
        IN.error("IN_ERROR");

        OUT.trace("OUT_TRACE");
        OUT.debug("OUT_DEBUG");
        OUT.info("OUT_INFO");
        OUT.warn("OUT_WARN");
        OUT.error("OUT_ERROR");

        log.trace("OTHER_TRACE");
        log.debug("OTHER_DEBUG");
        log.info("OTHER_INFO");
        log.warn("OTHER_WARN");
        log.error("OTHER_ERROR");

        // Sleep is needed to stall the main process from terminating
        // so logger can create the files and write the data
        Thread.sleep(3_000);
    }
}
