package dev.dutchy.runelite.libs.ui.button;

import dev.dutchy.runelite.libs.ui.image.ItemImageOptions;
import dev.dutchy.runelite.libs.ui.image.ItemImageProvider;
import dev.dutchy.runelite.libs.ui.image.ItemImageRequest;
import dev.dutchy.runelite.libs.ui.image.LoadedItemImage;
import dev.dutchy.runelite.libs.ui.item.ItemReference;
import dev.dutchy.runelite.libs.ui.item.ResolvedItem;
import dev.dutchy.runelite.libs.ui.item.StaticItemResolver;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefaultItemLoaderTest {

    private static final ResolvedItem COINS = ResolvedItem.of(995, "Coins");

    private final AtomicReference<ItemImageRequest> requested = new AtomicReference<>();
    private final ItemImageProvider provider = request -> {
        requested.set(request);
        return new LoadedItemImage(new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB));
    };
    private final DefaultItemLoader loader = new DefaultItemLoader(StaticItemResolver.of(COINS), provider);

    @Test
    void resolvesThenRequestsSpriteWithOptions() {
        Optional<LoadedItem> loaded = loader.load(ItemReference.byName("coins"), new ItemImageOptions(500, true)).join();
        assertEquals(COINS, loaded.orElseThrow().item());
        assertEquals(new ItemImageRequest(COINS.id(), 500, true), requested.get());
    }

    @Test
    void unresolvedReferenceSkipsImageProvider() {
        assertTrue(loader.load(ItemReference.byId(1), ItemImageOptions.DEFAULT).join().isEmpty());
        assertNull(requested.get());
    }
}
