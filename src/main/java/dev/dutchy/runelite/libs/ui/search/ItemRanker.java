package dev.dutchy.runelite.libs.ui.search;

import dev.dutchy.runelite.libs.ui.item.ResolvedItem;
import lombok.Value;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;
import java.util.stream.Collectors;

/** Orders catalogue entries against a query. Ties break on shorter name, then lower id. */
@Singleton
public final class ItemRanker {

    private final NameScorer scorer;

    @Inject
    public ItemRanker(NameScorer scorer) {
        this.scorer = Objects.requireNonNull(scorer, "scorer");
    }

    public List<ResolvedItem> rank(ItemIndex index, String query, int limit) {
        Objects.requireNonNull(index, "index");
        Objects.requireNonNull(query, "query");
        if (limit <= 0) {
            throw new IllegalArgumentException("Limit must be positive, got " + limit);
        }
        @Value
        class Scored {
            ResolvedItem item;
            int score;

        }
        String needle = ItemNames.normalize(query);
        Comparator<Scored> best = Comparator.comparingInt(Scored::score).reversed()
                .thenComparingInt(scored -> scored.item().name().length())
                .thenComparing(scored -> scored.item().id());
        return index.entries().stream()
                .map(entry -> {
                    OptionalInt score = scorer.score(needle, entry.normalizedName());
                    return score.isPresent() ? new Scored(entry.item(), score.getAsInt()) : null;
                })
                .filter(Objects::nonNull)
                .sorted(best)
                .limit(limit)
                .map(Scored::item)
                .collect(Collectors.toList());
    }

    public Optional<ResolvedItem> best(ItemIndex index, String query) {
        return rank(index, query, 1).stream().findFirst();
    }
}
