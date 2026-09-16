package dev.dutchy.runelite.libs.ui.search;

import dev.dutchy.runelite.libs.ui.item.ItemId;
import dev.dutchy.runelite.libs.ui.item.ItemReference;
import dev.dutchy.runelite.libs.ui.item.ItemResolver;
import dev.dutchy.runelite.libs.ui.item.ResolvedItem;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;

@Singleton
public final class FuzzyItemSearch implements ItemSearch {

    private final ItemIndexProvider index;
    private final ItemRanker ranker;
    private final ItemResolver resolver;
    private final Executor executor;

    @Inject
    public FuzzyItemSearch(ItemIndexProvider index, ItemRanker ranker, ItemResolver resolver, ScheduledExecutorService executor) {
        this(index, ranker, resolver, (Executor) executor);
    }

    public FuzzyItemSearch(ItemIndexProvider index, ItemRanker ranker, ItemResolver resolver, Executor executor) {
        this.index = Objects.requireNonNull(index, "index");
        this.ranker = Objects.requireNonNull(ranker, "ranker");
        this.resolver = Objects.requireNonNull(resolver, "resolver");
        this.executor = Objects.requireNonNull(executor, "executor");
    }

    @Override
    public CompletableFuture<List<ResolvedItem>> search(String query, int limit) {
        Objects.requireNonNull(query, "query");
        if (limit <= 0) {
            throw new IllegalArgumentException("Limit must be positive, got " + limit);
        }
        String trimmed = query.strip();
        if (trimmed.isEmpty()) {
            return CompletableFuture.completedFuture(List.of());
        }

        CompletableFuture<List<ResolvedItem>> byName = CompletableFuture.supplyAsync(() -> ranker.rank(index.index(), trimmed, limit), executor);
        Optional<ItemId> id = parseId(trimmed);
        if (id.isEmpty()) {
            return byName;
        }
        CompletableFuture<Optional<ResolvedItem>> byId = resolver.resolve(ItemReference.byId(id.get()))
                .exceptionally(error -> Optional.empty());
        return byId.thenCombine(byName, (idHit, nameHits) -> idHit.map(hit -> merge(hit, nameHits, limit)).orElse(nameHits));
    }

    /** The exact id match first, then the name matches without it, up to the limit. */
    private static List<ResolvedItem> merge(ResolvedItem idHit, List<ResolvedItem> nameHits, int limit) {
        List<ResolvedItem> merged = new ArrayList<>(limit);
        merged.add(idHit);
        for (ResolvedItem item : nameHits) {
            if (merged.size() >= limit) {
                break;
            }
            if (!item.id().equals(idHit.id())) {
                merged.add(item);
            }
        }
        return List.copyOf(merged);
    }

    private static Optional<ItemId> parseId(String query) {
        if (query.isEmpty() || query.length() > 9 || !query.chars().allMatch(Character::isDigit)) {
            return Optional.empty();
        }
        return Optional.of(ItemId.of(Integer.parseInt(query)));
    }
}
