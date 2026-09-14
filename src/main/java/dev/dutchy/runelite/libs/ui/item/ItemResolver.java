package dev.dutchy.runelite.libs.ui.item;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@FunctionalInterface
public interface ItemResolver {

    /** Empty when nothing matches. May complete on any thread. */
    CompletableFuture<Optional<ResolvedItem>> resolve(ItemReference reference);
}
