package dev.dutchy.runelite.libs.ui.search;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Singleton
/* Builds the index in the background, retrying until it is non-empty. */
public final class ItemIndexWarmUp {

    private static final Logger log = LoggerFactory.getLogger(ItemIndexWarmUp.class);

    public static final long DEFAULT_RETRY_DELAY_MILLIS = 2_000L;
    public static final int DEFAULT_MAX_ATTEMPTS = 60;

    private final ItemIndexProvider index;
    private final ScheduledExecutorService executor;
    private final long retryDelayMillis;
    private final int maxAttempts;

    private ScheduledFuture<?> scheduled;
    private int attempts;

    @Inject
    public ItemIndexWarmUp(ItemIndexProvider index, ScheduledExecutorService executor) {
        this(index, executor, DEFAULT_RETRY_DELAY_MILLIS, DEFAULT_MAX_ATTEMPTS);
    }

    public ItemIndexWarmUp(ItemIndexProvider index, ScheduledExecutorService executor, long retryDelayMillis, int maxAttempts) {
        this.index = Objects.requireNonNull(index, "index");
        this.executor = Objects.requireNonNull(executor, "executor");
        if (retryDelayMillis <= 0) {
            throw new IllegalArgumentException("Retry delay must be positive, got " + retryDelayMillis);
        }
        if (maxAttempts <= 0) {
            throw new IllegalArgumentException("Max attempts must be positive, got " + maxAttempts);
        }
        this.retryDelayMillis = retryDelayMillis;
        this.maxAttempts = maxAttempts;
    }

    public synchronized void start() {
        if (scheduled != null && !scheduled.isDone()) {
            return;
        }
        attempts = 0;
        scheduled = executor.scheduleWithFixedDelay(this::attempt, 0, retryDelayMillis, TimeUnit.MILLISECONDS);
    }

    public synchronized void stop() {
        if (scheduled != null) {
            scheduled.cancel(false);
            scheduled = null;
        }
    }

    private void attempt() {
        attempts++;
        boolean warm;
        try {
            warm = index.warmUp();
        } catch (RuntimeException e) {
            log.warn("Item index warm-up attempt {} failed", attempts, e);
            warm = false;
        }
        if (warm) {
            log.debug("Item index warm after {} attempt(s)", attempts);
            stop();
        } else if (attempts >= maxAttempts) {
            log.warn("Giving up warming the item index after {} attempts; searches will build it on demand", attempts);
            stop();
        }
    }
}
