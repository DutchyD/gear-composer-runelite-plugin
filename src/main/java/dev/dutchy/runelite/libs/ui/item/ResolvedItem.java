package dev.dutchy.runelite.libs.ui.item;

import lombok.Value;
import java.util.Objects;

@Value
public class ResolvedItem {
    ItemId id;
    String name;

    public ResolvedItem(ItemId id, String name) {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(name, "name");
        if (name.isBlank()) {
            throw new IllegalArgumentException("Item name must not be blank");
        }
        this.id = id;
        this.name = name;
    }

    public static ResolvedItem of(int id, String name) {
        return new ResolvedItem(ItemId.of(id), name);
    }
}
