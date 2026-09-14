package dev.dutchy.runelite.gear.bank;

import dev.dutchy.runelite.gear.content.SetupItem;
import dev.dutchy.runelite.libs.ui.item.ItemId;
import lombok.Value;
import lombok.experimental.Accessors;

import java.util.Objects;
import java.util.Optional;

/**
 * One bank slot to draw: the setup item and what the bank can supply for it. An empty supply makes
 * the slot a placeholder.
 */
@Value
@Accessors(fluent = true)
public class BankSlotPlan {
    int slotIndex;
    SetupItem item;
    SlotSupply supply;

    public BankSlotPlan(int slotIndex, SetupItem item, SlotSupply supply) {
        Objects.requireNonNull(item, "item");
        Objects.requireNonNull(supply, "supply");
        if (slotIndex < 0) {
            throw new IllegalArgumentException("Slot index must be non-negative, got " + slotIndex);
        }
        this.slotIndex = slotIndex;
        this.item = item;
        this.supply = supply;
    }

    /** The id the bank holds for the item, when it holds any. */
    public Optional<ItemId> resolved() {
        return supply.shown().map(SlotSupply.Stack::id);
    }

    public boolean placeholder() {
        return supply.isEmpty();
    }

    /** The id to draw: what the bank holds, or the item itself when ghosted. */
    public ItemId shown() {
        return resolved().orElse(item.id());
    }
}
