package dev.dutchy.runelite.libs.ui.search;

import dev.dutchy.runelite.libs.ui.item.ResolvedItem;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ItemIndexTest {

    @Test
    void precomputesNormalisedNames() {
        ItemIndex index = ItemIndex.of(List.of(ResolvedItem.of(4151, "Abyssal Whip")));
        assertEquals("abyssal whip", index.entries().get(0).normalizedName());
    }

    @Test
    void reportsSizeAndEmptiness() {
        assertTrue(ItemIndex.EMPTY.isEmpty());
        assertEquals(0, ItemIndex.EMPTY.size());
        assertEquals(2, ItemIndex.of(List.of(ResolvedItem.of(1, "a"), ResolvedItem.of(2, "b"))).size());
    }

    @Test
    void entriesAreImmutable() {
        List<IndexedItem> entries = ItemIndex.of(List.of(ResolvedItem.of(1, "a"))).entries();
        assertThrows(UnsupportedOperationException.class, entries::clear);
    }
}
