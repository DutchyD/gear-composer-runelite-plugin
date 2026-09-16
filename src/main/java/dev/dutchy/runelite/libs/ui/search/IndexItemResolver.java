package dev.dutchy.runelite.libs.ui.search;

import dev.dutchy.runelite.libs.ui.item.ItemReference;
import dev.dutchy.runelite.libs.ui.item.ItemResolver;
import dev.dutchy.runelite.libs.ui.item.ResolvedItem;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/** Resolves names against the full item catalogue. Ids are left to a resolver that reads the game. */
@Singleton
public final class IndexItemResolver implements ItemResolver {

    private final ItemIndexProvider index;
    private final ItemRanker ranker;

    @Inject
    public IndexItemResolver(ItemIndexProvider index, ItemRanker ranker) {
        this.index = Objects.requireNonNull(index, "index");
        this.ranker = Objects.requireNonNull(ranker, "ranker");
    }

    @Override
    public CompletableFuture<Optional<ResolvedItem>> resolve(ItemReference reference) {
        Objects.requireNonNull(reference, "reference");
        if (reference instanceof ItemReference.ByName) {
            ItemReference.ByName byName = (ItemReference.ByName) reference;
            return CompletableFuture.completedFuture(ranker.best(index.index(), byName.name()));
        }
        return CompletableFuture.completedFuture(Optional.empty());
    }
}
