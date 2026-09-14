package dev.dutchy.runelite.libs.ui.search;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ItemIndexWarmUpTest {

    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    @AfterEach
    void tearDown() {
        executor.shutdownNow();
    }

    private static final class CountingProvider implements ItemIndexProvider {

        final AtomicInteger attempts = new AtomicInteger();
        final int attemptsUntilWarm;
        volatile boolean throwOnce;

        CountingProvider(int attemptsUntilWarm) {
            this.attemptsUntilWarm = attemptsUntilWarm;
        }

        @Override
        public ItemIndex index() {
            return ItemIndex.EMPTY;
        }

        @Override
        public boolean warmUp() {
            int attempt = attempts.incrementAndGet();
            if (throwOnce) {
                throwOnce = false;
                throw new IllegalStateException("catalogue exploded");
            }
            return attempt >= attemptsUntilWarm;
        }
    }

    @SuppressWarnings("BusyWait")
    private static void awaitAtLeast(AtomicInteger counter, int target) throws InterruptedException {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
        while (counter.get() < target && System.nanoTime() < deadline) {
            Thread.sleep(5);
        }
        assertTrue(counter.get() >= target, "expected at least " + target + " attempts, saw " + counter.get());
    }

    @Test
    void retriesUntilTheIndexIsWarmThenStops() throws Exception {
        CountingProvider provider = new CountingProvider(3);
        ItemIndexWarmUp warmUp = new ItemIndexWarmUp(provider, executor, 10, 50);

        warmUp.start();
        awaitAtLeast(provider.attempts, 3);
        Thread.sleep(150);

        assertEquals(3, provider.attempts.get(), "no further attempts once warm");
    }

    @Test
    void givesUpAfterTheAttemptBudget() throws Exception {
        CountingProvider provider = new CountingProvider(Integer.MAX_VALUE);
        ItemIndexWarmUp warmUp = new ItemIndexWarmUp(provider, executor, 5, 4);

        warmUp.start();
        awaitAtLeast(provider.attempts, 4);
        Thread.sleep(120);

        assertEquals(4, provider.attempts.get(), "stops at the budget");
    }

    @Test
    void failedAttemptDoesNotStopTheSchedule() throws Exception {
        CountingProvider provider = new CountingProvider(3);
        provider.throwOnce = true;
        ItemIndexWarmUp warmUp = new ItemIndexWarmUp(provider, executor, 10, 50);

        warmUp.start();
        awaitAtLeast(provider.attempts, 3);
        Thread.sleep(150);

        assertEquals(3, provider.attempts.get());
    }

    @Test
    void stopHaltsRetriesAndIsSafeWhenNotStarted() throws Exception {
        CountingProvider provider = new CountingProvider(Integer.MAX_VALUE);
        ItemIndexWarmUp warmUp = new ItemIndexWarmUp(provider, executor, 10, 1000);
        warmUp.stop();

        warmUp.start();
        awaitAtLeast(provider.attempts, 2);
        warmUp.stop();
        int afterStop = provider.attempts.get();
        Thread.sleep(120);

        assertTrue(provider.attempts.get() <= afterStop + 1, "retries halted");
    }

    @Test
    void startIsIdempotent() throws Exception {
        CountingProvider provider = new CountingProvider(Integer.MAX_VALUE);
        ItemIndexWarmUp warmUp = new ItemIndexWarmUp(provider, executor, 40, 1000);

        warmUp.start();
        warmUp.start();
        Thread.sleep(100);
        warmUp.stop();

        assertTrue(provider.attempts.get() <= 4, "one schedule, not two; saw " + provider.attempts.get());
    }

    @Test
    void validatesConstructorArguments() {
        CountingProvider provider = new CountingProvider(1);
        assertThrows(IllegalArgumentException.class, () -> new ItemIndexWarmUp(provider, executor, 0, 10));
        assertThrows(IllegalArgumentException.class, () -> new ItemIndexWarmUp(provider, executor, 10, 0));
    }
}
