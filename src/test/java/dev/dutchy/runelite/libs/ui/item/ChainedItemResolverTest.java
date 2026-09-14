package dev.dutchy.runelite.libs.ui.item;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChainedItemResolverTest {

    private static final ResolvedItem WHIP = ResolvedItem.of(4151, "Abyssal whip");
    private static final ResolvedItem FIRE_CAPE = ResolvedItem.of(6570, "Fire cape");

    @Test
    void firstHitWins() {
        ItemResolver chain = ChainedItemResolver.of(StaticItemResolver.of(WHIP), StaticItemResolver.of(FIRE_CAPE));
        assertEquals(Optional.of(FIRE_CAPE), chain.resolve(ItemReference.byId(6570)).join());
        assertEquals(Optional.of(WHIP), chain.resolve(ItemReference.byId(4151)).join());
    }

    @Test
    void laterResolversAreNotConsultedAfterAHit() {
        AtomicInteger calls = new AtomicInteger();
        ItemResolver counting = ref -> {
            calls.incrementAndGet();
            return CompletableFuture.completedFuture(Optional.empty());
        };
        ItemResolver chain = ChainedItemResolver.of(StaticItemResolver.of(WHIP), counting);
        chain.resolve(ItemReference.byId(4151)).join();
        assertEquals(0, calls.get());
    }

    @Test
    void allMissesYieldEmpty() {
        ItemResolver chain = ChainedItemResolver.of(StaticItemResolver.of(WHIP), StaticItemResolver.of(FIRE_CAPE));
        assertTrue(chain.resolve(ItemReference.byName("Dragon claws")).join().isEmpty());
    }

    @Test
    void requiresAtLeastOneDelegate() {
        assertThrows(IllegalArgumentException.class, () -> new ChainedItemResolver(List.of()));
    }
}
