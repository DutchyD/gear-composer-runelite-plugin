package dev.dutchy.runelite.gear.bank;

import dev.dutchy.runelite.libs.ui.item.ItemId;

/** What an item is called, as a bank build needs it: at once, or not at all. */
@FunctionalInterface
public interface BankItemNames {

    /** The game's name for the item, or a stand-in naming its id when the game has none. */
    String of(ItemId item);

    static String unknown(ItemId item) {
        return "Item " + item.value();
    }
}
