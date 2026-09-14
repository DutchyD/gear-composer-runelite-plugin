package dev.dutchy.runelite.libs.ui.search;

import dev.dutchy.runelite.libs.ui.item.ResolvedItem;

import java.util.Collection;
import java.util.List;

@FunctionalInterface
public interface ItemCatalog {

    List<ResolvedItem> items();

    static ItemCatalog of(Collection<ResolvedItem> items) {
        List<ResolvedItem> snapshot = List.copyOf(items);
        return () -> snapshot;
    }
}
