package dev.dutchy.runelite.gear.content;

import dev.dutchy.runelite.libs.ui.item.ItemId;

import java.util.Collection;
import java.util.Optional;

/** Which equipment slot an item is worn in, when the game data says. Must be safe to call from the EDT. */
public interface EquipmentSlots {

    /** Empty when unknown, including while the answer is still being looked up. */
    Optional<EquipmentSlot> slotOf(ItemId item);

    /** Starts looking items up ahead of time so later answers are immediate. */
    default void warmUp(Collection<ItemId> items) {
    }

    /** Knows nothing, so every drop onto equipment is allowed. */
    static EquipmentSlots unknown() {
        return item -> Optional.empty();
    }
}
