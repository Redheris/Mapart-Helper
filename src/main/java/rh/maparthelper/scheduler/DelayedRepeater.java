package rh.maparthelper.scheduler;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class DelayedRepeater {
    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> repeatFuture;
    private long timeLeft;

    public void start(Runnable action, long delayMs, long repeatPeriodMs) {
        stop();
        timeLeft = System.currentTimeMillis();
        action.run();

        repeatFuture = scheduler.scheduleAtFixedRate(() -> {
            if (System.currentTimeMillis() - timeLeft >= delayMs) {
                action.run();
            }
        }, repeatPeriodMs, repeatPeriodMs, TimeUnit.MILLISECONDS);
    }

    public void stop() {
        if (repeatFuture != null) {
            repeatFuture.cancel(true);
            repeatFuture = null;
        }
    }
}
