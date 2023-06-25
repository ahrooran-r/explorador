package lk.uom.dc;

import static lk.uom.dc.log.LogManager.APP;

/**
 * Hello world!
 */
public class App {
    public static void main(String[] args) throws InterruptedException {
        APP.trace("APP_TRACE");
        APP.debug("APP_DEBUG");
        APP.info("APP_INFO");
        APP.warn("APP_WARN");
        APP.error("APP_ERROR");

        Thread.sleep(3000);
    }
}
