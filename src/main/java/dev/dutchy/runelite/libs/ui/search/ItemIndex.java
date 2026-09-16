package dev.dutchy.runelite.libs.ui.search;

import dev.dutchy.runelite.libs.ui.item.ResolvedItem;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public final class ItemIndex {

    public static final ItemIndex EMPTY = new ItemIndex(List.of());

    private final List<IndexedItem> entries;

    private ItemIndex(List<IndexedItem> entries) {
        this.entries = List.copyOf(entries);
    }

    public static ItemIndex of(Collection<ResolvedItem> items) {
        Objects.requireNonNull(items, "items");
        return new ItemIndex(items.stream().map(IndexedItem::of).collect(Collectors.toList()));
    }

    public List<IndexedItem> entries() {
        return entries;
    }

    public boolean isEmpty() {
        return entries.isEmpty();
    }

    public int size() {
        return entries.size();
    }

    @Override
    public String toString() {
        return "ItemIndex[" + entries.size() + " items]";
    }
}
