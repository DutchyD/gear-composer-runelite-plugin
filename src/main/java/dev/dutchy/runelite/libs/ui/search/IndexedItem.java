package dev.dutchy.runelite.libs.ui.search;

import dev.dutchy.runelite.libs.ui.item.ResolvedItem;
import lombok.Value;
import lombok.experimental.Accessors;

import java.util.Objects;

@Value
@Accessors(fluent = true)
public class IndexedItem {
    ResolvedItem item;
    String normalizedName;

    public IndexedItem(ResolvedItem item, String normalizedName) {
        Objects.requireNonNull(item, "item");
        Objects.requireNonNull(normalizedName, "normalizedName");
        this.item = item;
        this.normalizedName = normalizedName;
    }

    public static IndexedItem of(ResolvedItem item) {
        return new IndexedItem(item, ItemNames.normalize(item.name()));
    }
}
