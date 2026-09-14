package dev.dutchy.runelite.libs.ui.item;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StaticItemResolverTest {

    private static final ResolvedItem WHIP = ResolvedItem.of(4151, "Abyssal whip");
    private static final ResolvedItem FIRE_CAPE = ResolvedItem.of(6570, "Fire cape");

    private final StaticItemResolver resolver = StaticItemResolver.of(WHIP, FIRE_CAPE);

    @Test
    void resolvesById() {
        assertEquals(Optional.of(FIRE_CAPE), resolver.resolve(ItemReference.byId(6570)).join());
    }

    @Test
    void resolvesByNameCaseInsensitively() {
        assertEquals(Optional.of(WHIP), resolver.resolve(ItemReference.byName("abyssal WHIP")).join());
    }

    @Test
    void fallsBackToFuzzyNameMatch() {
        assertEquals(Optional.of(FIRE_CAPE), resolver.resolve(ItemReference.byName("fire")).join());
    }

    @Test
    void unknownItemsResolveEmpty() {
        assertTrue(resolver.resolve(ItemReference.byId(1)).join().isEmpty());
        assertTrue(resolver.resolve(ItemReference.byName("Dragon claws")).join().isEmpty());
    }
}
