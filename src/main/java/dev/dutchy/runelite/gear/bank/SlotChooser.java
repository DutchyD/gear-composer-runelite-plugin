package dev.dutchy.runelite.gear.bank;

import dev.dutchy.runelite.libs.ui.item.ItemId;

/** Where a player's choice of which family member a slot draws is sent. */
@FunctionalInterface
public interface SlotChooser {

    /** Draws the given stack in the slot from now on, until the layout is taken down. */
    void show(int slotIndex, ItemId id);
}
