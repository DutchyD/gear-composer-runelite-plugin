package dev.dutchy.runelite.libs.ui.search;

import dev.dutchy.runelite.libs.ui.item.ResolvedItem;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CachingItemIndexTest {

    private static final List<ResolvedItem> ITEMS = List.of(
            ResolvedItem.of(4151, "Abyssal whip"),
            ResolvedItem.of(995, "Coins"));

    private static final class LateCatalog implements ItemCatalog {

        final AtomicInteger calls = new AtomicInteger();
        volatile int emptyResponses;

        @Override
        public List<ResolvedItem> items() {
            return calls.incrementAndGet() <= emptyResponses ? List.of() : ITEMS;
        }
    }

    @Test
    void buildsOnceAndServesFromMemory() {
        LateCatalog catalog = new LateCatalog();
        CachingItemIndex index = new CachingItemIndex(catalog);

        ItemIndex first = index.index();
        ItemIndex second = index.index();

        assertSame(first, second);
        assertEquals(2, first.size());
        assertEquals(1, catalog.calls.get(), "catalogue read once");
    }

    @Test
    void doesNotCacheAnEmptyCatalogue() {
        LateCatalog catalog = new LateCatalog();
        catalog.emptyResponses = 2;
        CachingItemIndex index = new CachingItemIndex(catalog);

        assertTrue(index.index().isEmpty());
        assertFalse(index.isWarm());
        assertTrue(index.index().isEmpty());
        assertFalse(index.isWarm());

        assertEquals(2, index.index().size(), "third read succeeds once prices arrive");
        assertTrue(index.isWarm());
    }

    @Test
    void warmUpReportsWhenTheIndexHasContent() {
        LateCatalog catalog = new LateCatalog();
        catalog.emptyResponses = 1;
        CachingItemIndex index = new CachingItemIndex(catalog);

        assertFalse(index.warmUp(), "prices not downloaded yet");
        assertTrue(index.warmUp());
    }

    @Test
    void concurrentColdReadsBuildOnlyOnce() throws Exception {
        LateCatalog catalog = new LateCatalog();
        CachingItemIndex index = new CachingItemIndex(catalog);
        int threads = 8;
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threads);

        for (int i = 0; i < threads; i++) {
            new Thread(() -> {
                try {
                    start.await();
                    index.index();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    done.countDown();
                }
            }).start();
        }
        start.countDown();
        assertTrue(done.await(10, TimeUnit.SECONDS));
        assertEquals(1, catalog.calls.get(), "single-flighted");
    }
}
