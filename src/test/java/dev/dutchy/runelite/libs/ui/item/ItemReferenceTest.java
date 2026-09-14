package dev.dutchy.runelite.libs.ui.item;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ItemReferenceTest {

    @Test
    void byNameTrimsWhitespace() {
        ItemReference.ByName ref = (ItemReference.ByName) ItemReference.byName("  Abyssal whip ");
        assertEquals("Abyssal whip", ref.name());
    }

    @Test
    void byNameRejectsBlank() {
        assertThrows(IllegalArgumentException.class, () -> ItemReference.byName("   "));
    }

    @Test
    void byIdRejectsNegative() {
        assertThrows(IllegalArgumentException.class, () -> ItemReference.byId(-5));
    }

    @Test
    void describesBothVariants() {
        assertEquals("item #4151", ItemReference.byId(4151).describe());
        assertEquals("\"Coins\"", ItemReference.byName("Coins").describe());
    }

    @Test
    void referencesAreValueObjects() {
        assertEquals(ItemReference.byId(1), ItemReference.byId(ItemId.of(1)));
        assertEquals(ItemReference.byName("a"), ItemReference.byName(" a "));
    }
}
