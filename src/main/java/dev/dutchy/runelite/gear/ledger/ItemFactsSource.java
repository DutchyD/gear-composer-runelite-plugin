package dev.dutchy.runelite.gear.ledger;

import dev.dutchy.runelite.libs.ui.item.ItemId;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/** Item facts that may arrive late: ask, and listen for the moment more become known. Safe from the EDT. */
public interface ItemFactsSource {

    /** Empty while unknown, including while a lookup is still under way. */
    Optional<ItemFacts> factsOf(ItemId item);

    default void warmUp(Collection<ItemId> items) {
    }

    /** Called on the EDT whenever facts that were unknown become known. */
    default void addListener(Runnable listener) {
    }

    static ItemFactsSource none() {
        return item -> Optional.empty();
    }

    static ItemFactsSource fixed(Map<ItemId, ItemFacts> facts) {
        Map<ItemId, ItemFacts> copy = Map.copyOf(Objects.requireNonNull(facts, "facts"));
        return item -> Optional.ofNullable(copy.get(item));
    }
}
