package dev.dutchy.runelite.gear.items;

import dev.dutchy.runelite.libs.ui.item.ItemId;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/** A hand-written note table, for tests. */
public final class StaticItemNotes implements ItemNotes {

    private final Map<ItemId, ItemId> unnoted = new HashMap<>();

    public StaticItemNotes note(int notedId, int unnotedId) {
        unnoted.put(ItemId.of(notedId), ItemId.of(unnotedId));
        return this;
    }

    @Override
    public Optional<ItemId> unnotedOf(ItemId item) {
        return Optional.ofNullable(unnoted.get(Objects.requireNonNull(item, "item")));
    }
}
