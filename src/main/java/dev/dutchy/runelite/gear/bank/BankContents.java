package dev.dutchy.runelite.gear.bank;

import dev.dutchy.runelite.libs.ui.item.ItemId;

import java.util.Map;
import java.util.Objects;

/** What the player actually holds in the bank. */
@FunctionalInterface
public interface BankContents {

    boolean contains(ItemId item);

    /** How many of the item the bank holds; a source that only knows presence reports one. */
    default int count(ItemId item) {
        return contains(item) ? 1 : 0;
    }

    static BankContents nothing() {
        return item -> false;
    }

    /** A bank holding exactly these stacks. */
    static BankContents of(Map<ItemId, Integer> stacks) {
        Objects.requireNonNull(stacks, "stacks");
        return new BankContents() {
            @Override
            public boolean contains(ItemId item) {
                return count(item) > 0;
            }

            @Override
            public int count(ItemId item) {
                return stacks.getOrDefault(Objects.requireNonNull(item, "item"), 0);
            }
        };
    }
}
