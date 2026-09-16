package dev.dutchy.runelite.libs.ui.search;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Objects;

@Singleton
/* Never caches an empty catalogue. */
public final class CachingItemIndex implements ItemIndexProvider {

    private static final Logger log = LoggerFactory.getLogger(CachingItemIndex.class);

    private final ItemCatalog catalog;
    private final Object buildLock = new Object();
    private volatile ItemIndex cached = ItemIndex.EMPTY;

    @Inject
    public CachingItemIndex(ItemCatalog catalog) {
        this.catalog = Objects.requireNonNull(catalog, "catalog");
    }

    @Override
    public ItemIndex index() {
        ItemIndex snapshot = cached;
        if (!snapshot.isEmpty()) {
            return snapshot;
        }
        synchronized (buildLock) {
            if (!cached.isEmpty()) {
                return cached;
            }
            long startedAt = System.nanoTime();
            ItemIndex built = ItemIndex.of(catalog.items());
            if (built.isEmpty()) {
                return ItemIndex.EMPTY;
            }
            cached = built;
            log.debug("Indexed {} items in {} ms", built.size(), (System.nanoTime() - startedAt) / 1_000_000);
            return built;
        }
    }

    public boolean isWarm() {
        return !cached.isEmpty();
    }
}
