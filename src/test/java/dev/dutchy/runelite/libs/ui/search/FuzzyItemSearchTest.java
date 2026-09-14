package dev.dutchy.runelite.libs.ui.search;

import dev.dutchy.runelite.libs.ui.item.ResolvedItem;
import dev.dutchy.runelite.libs.ui.item.StaticItemResolver;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FuzzyItemSearchTest {

    private static final ResolvedItem WHIP = ResolvedItem.of(4151, "Abyssal whip");
    private static final ResolvedItem D_SCIM = ResolvedItem.of(4587, "Dragon scimitar");
    private static final ResolvedItem R_SCIM = ResolvedItem.of(1333, "Rune scimitar");
    private static final ResolvedItem DDS = ResolvedItem.of(1215, "Dragon dagger");
    private static final ResolvedItem FIRE_CAPE = ResolvedItem.of(6570, "Fire cape");
    private static final ResolvedItem COINS = ResolvedItem.of(995, "Coins");
    private static final ResolvedItem POTION = ResolvedItem.of(2434, "Prayer potion(4)");

    private final ItemIndexProvider index = new CachingItemIndex(ItemCatalog.of(List.of(WHIP, D_SCIM, R_SCIM, DDS, COINS, POTION)));
    private final StaticItemResolver resolver = StaticItemResolver.of(WHIP, D_SCIM, R_SCIM, DDS, COINS, POTION, FIRE_CAPE);
    private final FuzzyItemSearch search = new FuzzyItemSearch(index, new ItemRanker(new SubsequenceNameScorer()), resolver, Runnable::run);

    @Test
    void ranksBestNameMatchesFirst() {
        List<ResolvedItem> hits = search.search("scim", 10).join();
        assertEquals(List.of(R_SCIM, D_SCIM), hits);
    }

    @Test
    void abbreviationsFindItems() {
        assertEquals(List.of(D_SCIM), search.search("dscim", 10).join());
    }

    @Test
    void respectsLimit() {
        assertEquals(1, search.search("dragon", 1).join().size());
    }

    @Test
    void numericQueryResolvesIdEvenWhenAbsentFromCatalog() {
        assertEquals(List.of(FIRE_CAPE), search.search("6570", 10).join());
    }

    @Test
    void numericQueryPutsIdHitFirstAndDeduplicates() {
        List<ResolvedItem> hits = search.search("4", 10).join();
        assertEquals(POTION, hits.get(0), "id 4 does not exist, so name hits only");
        List<ResolvedItem> coinHits = search.search("995", 10).join();
        assertEquals(COINS, coinHits.get(0));
        assertEquals(1, coinHits.stream().filter(COINS::equals).count());
    }

    @Test
    void blankQueryYieldsNothing() {
        assertTrue(search.search("   ", 10).join().isEmpty());
    }

    @Test
    void rejectsNonPositiveLimit() {
        assertThrows(IllegalArgumentException.class, () -> search.search("whip", 0));
    }
}
