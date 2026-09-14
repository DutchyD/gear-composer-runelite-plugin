package dev.dutchy.runelite.libs.ui.search;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ItemDefinitionsTest {

    private static final int NONE = -1;
    private static final int A_PLACEHOLDER = 14401;

    @Test
    void bankableItemsAreListed() {
        assertTrue(ItemDefinitions.isBankable("Abyssal whip", NONE, NONE, A_PLACEHOLDER));
    }

    @Test
    void untradeableItemsAreListedAsLongAsTheyCanBeBanked() {
        assertTrue(ItemDefinitions.isBankable("Barrows gloves", NONE, NONE, A_PLACEHOLDER));
        assertTrue(ItemDefinitions.isBankable("Fire cape", NONE, NONE, A_PLACEHOLDER));
    }

    @Test
    void copiesThatCannotBeBankedAreSkipped() {
        assertFalse(ItemDefinitions.isBankable("Rune scimitar", NONE, NONE, NONE),
                "minigame and other closed-content copies share a name but have no placeholder");
    }

    @Test
    void notedCopiesAreSkipped() {
        assertFalse(ItemDefinitions.isBankable("Abyssal whip", 4151, NONE, A_PLACEHOLDER));
    }

    @Test
    void placeholdersThemselvesAreSkipped() {
        assertFalse(ItemDefinitions.isBankable("Abyssal whip", NONE, A_PLACEHOLDER, 4151));
    }

    @Test
    void unusedCacheEntriesAreSkipped() {
        assertFalse(ItemDefinitions.isBankable("null", NONE, NONE, A_PLACEHOLDER));
        assertFalse(ItemDefinitions.isBankable("", NONE, NONE, A_PLACEHOLDER));
        assertFalse(ItemDefinitions.isBankable("   ", NONE, NONE, A_PLACEHOLDER));
        assertFalse(ItemDefinitions.isBankable(null, NONE, NONE, A_PLACEHOLDER));
    }
}
