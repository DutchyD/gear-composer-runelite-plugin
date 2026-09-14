package dev.dutchy.runelite.gear.items;

import dev.dutchy.runelite.libs.ui.item.ItemId;

import java.util.Collection;
import java.util.List;

/** Knows which item ids belong to the same family: charges, doses, degrade states, imbues. */
@FunctionalInterface
public interface ItemVariants {

    /** Every id in the family, including {@code item} itself, in a stable order. */
    Collection<ItemId> familyOf(ItemId item);

    /** Treats every item as its own family. */
    static ItemVariants none() {
        return List::of;
    }
}
