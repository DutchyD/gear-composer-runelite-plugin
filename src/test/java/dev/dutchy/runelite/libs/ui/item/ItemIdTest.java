package dev.dutchy.runelite.libs.ui.item;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ItemIdTest {

    @Test
    void rejectsNegativeIds() {
        assertThrows(IllegalArgumentException.class, () -> ItemId.of(-1));
    }

    @Test
    void acceptsZero() {
        assertEquals(0, ItemId.of(0).value());
    }

    @Test
    void ordersNumerically() {
        assertTrue(ItemId.of(4151).compareTo(ItemId.of(995)) > 0);
    }
}
