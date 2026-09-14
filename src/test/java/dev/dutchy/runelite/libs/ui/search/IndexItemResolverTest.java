package dev.dutchy.runelite.libs.ui.search;

import dev.dutchy.runelite.libs.ui.item.ChainedItemResolver;
import dev.dutchy.runelite.libs.ui.item.ItemReference;
import dev.dutchy.runelite.libs.ui.item.ItemResolver;
import dev.dutchy.runelite.libs.ui.item.ResolvedItem;
import dev.dutchy.runelite.libs.ui.item.StaticItemResolver;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IndexItemResolverTest {

    private static final ResolvedItem WHIP = ResolvedItem.of(4151, "Abyssal whip");
    private static final ResolvedItem BARROWS_GLOVES = ResolvedItem.of(7462, "Barrows gloves");
    private static final ResolvedItem RUNE_GLOVES = ResolvedItem.of(7459, "Rune gloves");

    private final ItemIndexProvider index =
            new CachingItemIndex(ItemCatalog.of(List.of(WHIP, BARROWS_GLOVES, RUNE_GLOVES)));
    private final IndexItemResolver resolver = new IndexItemResolver(index, ItemSearches.ranker());

    @Test
    void resolvesUntradeableItemsByName() {
        assertEquals(Optional.of(BARROWS_GLOVES), resolver.resolve(ItemReference.byName("Barrows gloves")).join());
        assertEquals(Optional.of(RUNE_GLOVES), resolver.resolve(ItemReference.byName("rune gloves")).join());
    }

    @Test
    void picksTheBestMatchForAPartialName() {
        assertEquals(Optional.of(BARROWS_GLOVES), resolver.resolve(ItemReference.byName("barrows")).join());
    }

    @Test
    void unknownNamesResolveEmpty() {
        assertTrue(resolver.resolve(ItemReference.byName("Dragon claws")).join().isEmpty());
    }

    @Test
    void idsAreLeftToTheResolverThatReadsTheGame() {
        assertTrue(resolver.resolve(ItemReference.byId(4151)).join().isEmpty());
    }

    @Test
    void chainedWithAnIdResolverEachKindIsHandled() {
        ItemResolver byId = StaticItemResolver.of(WHIP);
        ItemResolver chain = ChainedItemResolver.of(byId, resolver);

        assertEquals(Optional.of(WHIP), chain.resolve(ItemReference.byId(4151)).join());
        assertEquals(Optional.of(BARROWS_GLOVES), chain.resolve(ItemReference.byName("Barrows gloves")).join());
    }
}
