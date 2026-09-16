package dev.dutchy.runelite.libs.ui.search;

import dev.dutchy.runelite.libs.ui.item.ResolvedItem;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@FunctionalInterface
public interface ItemSearch {

    /** Blank query yields no results. May complete on any thread. */
    CompletableFuture<List<ResolvedItem>> search(String query, int limit);
}
