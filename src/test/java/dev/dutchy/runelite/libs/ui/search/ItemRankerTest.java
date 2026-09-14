package dev.dutchy.runelite.libs.ui.search;

import dev.dutchy.runelite.libs.ui.item.ResolvedItem;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ItemRankerTest {

    private static final ResolvedItem RUNE_SCIMITAR = ResolvedItem.of(1333, "Rune scimitar");
    private static final ResolvedItem DRAGON_SCIMITAR = ResolvedItem.of(4587, "Dragon scimitar");
    private static final ResolvedItem RUNE_GLOVES = ResolvedItem.of(7459, "Rune gloves");

    private final ItemRanker ranker = ItemSearches.ranker();
    private final ItemIndex index = ItemIndex.of(List.of(DRAGON_SCIMITAR, RUNE_SCIMITAR, RUNE_GLOVES));

    @Test
    void ordersBetterMatchesFirst() {
        assertEquals(List.of(RUNE_SCIMITAR, DRAGON_SCIMITAR), ranker.rank(index, "scim", 10));
    }

    @Test
    void respectsTheLimit() {
        assertEquals(1, ranker.rank(index, "scim", 1).size());
    }

    @Test
    void bestReturnsTheTopMatch() {
        assertEquals(RUNE_GLOVES, ranker.best(index, "gloves").orElseThrow());
    }

    @Test
    void noMatchesYieldNothing() {
        assertTrue(ranker.rank(index, "nonsense", 10).isEmpty());
        assertTrue(ranker.best(index, "nonsense").isEmpty());
    }

    @Test
    void rejectsANonPositiveLimit() {
        assertThrows(IllegalArgumentException.class, () -> ranker.rank(index, "scim", 0));
    }
}
