package lk.uom.dc;

import java.time.Duration;

public class ThreadAssist {

    private ThreadAssist() {
    }

    /**
     * Sleep without throwing exceptions
     *
     * @param duration of sleep
     */
    public static void sleepQuiet(Duration duration) {
        try {
            Thread.sleep(duration.toMillis());
        } catch (InterruptedException ignored) {
        }
    }

    public static void sleepQuiet(long millis) {
        sleepQuiet(Duration.ofMillis(millis));
    }

    public static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ie) {
            ie.printStackTrace();
        }
    }
}
