package dev.dutchy.runelite.gear.items;

import dev.dutchy.runelite.libs.ui.item.ItemId;

import java.util.HashMap;
import java.util.Map;
import java.util.OptionalInt;

/** A hand-written charge table, for tests. */
public final class StaticItemCharges implements ItemCharges {

    private final Map<ItemId, Integer> charges = new HashMap<>();

    public StaticItemCharges charged(int id, int count) {
        charges.put(ItemId.of(id), count);
        return this;
    }

    @Override
    public OptionalInt chargesOf(ItemId item) {
        Integer count = charges.get(item);
        return count == null ? OptionalInt.empty() : OptionalInt.of(count);
    }
}
