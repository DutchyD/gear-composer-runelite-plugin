package dev.dutchy.runelite.gear.items;

import dev.dutchy.runelite.libs.ui.item.ItemId;

import java.util.Optional;

/** Links noted items to the real item they stand for. */
public interface ItemNotes {

    /** The unnoted item behind a note; empty when the id is not a note. */
    Optional<ItemId> unnotedOf(ItemId item);

    /** Treats nothing as a note. */
    static ItemNotes none() {
        return item -> Optional.empty();
    }
}
