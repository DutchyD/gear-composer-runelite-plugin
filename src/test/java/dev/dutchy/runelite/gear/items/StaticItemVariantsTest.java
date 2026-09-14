package dev.dutchy.runelite.gear.items;

import dev.dutchy.runelite.libs.ui.item.ItemId;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StaticItemVariantsTest {

    @Test
    void aFamilyListsTheAskedItemFirst() {
        ItemVariants variants = new StaticItemVariants().family(1, 2, 3);
        assertEquals(List.of(ItemId.of(2), ItemId.of(1), ItemId.of(3)), List.copyOf(variants.familyOf(ItemId.of(2))));
    }

    @Test
    void anUnknownItemIsItsOwnFamily() {
        assertEquals(List.of(ItemId.of(9)), List.copyOf(new StaticItemVariants().familyOf(ItemId.of(9))));
        assertEquals(List.of(ItemId.of(9)), List.copyOf(ItemVariants.none().familyOf(ItemId.of(9))));
    }
}
