package dev.dutchy.runelite.libs.ui.button;

import dev.dutchy.runelite.libs.ui.image.ItemImageOptions;
import dev.dutchy.runelite.libs.ui.item.ItemReference;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@FunctionalInterface
public interface ItemLoader {

    /** Empty when the reference matches nothing. May complete on any thread. */
    CompletableFuture<Optional<LoadedItem>> load(ItemReference reference, ItemImageOptions options);
}
