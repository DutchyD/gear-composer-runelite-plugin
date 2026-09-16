package dev.dutchy.runelite.gear.bank;

import java.util.*;

/**
 * The slots a bank build may reuse, and who gets which. An item the player holds keeps the slot that
 * already shows it, so the game still withdraws from the right place; any other slot can be borrowed
 * once, for a placeholder or a second copy. Slots are tracked by identity, never by equality, and
 * claimed slots are offered before spares so a borrow cannot take one the layout still needs.
 */
final class SlotPool<T> {

    private final Map<Integer, T> byItem = new HashMap<>();
    private final Deque<T> order = new ArrayDeque<>();
    private final Set<T> used = Collections.newSetFromMap(new IdentityHashMap<>());

    /** Offers a slot that currently shows the item. The first slot offered per item is its template. */
    void add(int itemId, T slot) {
        Objects.requireNonNull(slot, "slot");
        byItem.putIfAbsent(itemId, slot);
        order.add(slot);
    }

    /** The slot already showing the item, the first time it is asked for. */
    Optional<T> claim(int itemId) {
        T slot = byItem.get(itemId);
        return slot != null && used.add(slot) ? Optional.of(slot) : Optional.empty();
    }

    /** The slot showing the item, claimed or not, to copy an appearance from. */
    Optional<T> template(int itemId) {
        return Optional.ofNullable(byItem.get(itemId));
    }

    /** Any slot not yet handed out. */
    Optional<T> spare() {
        T slot;
        while ((slot = order.poll()) != null) {
            if (used.add(slot)) {
                return Optional.of(slot);
            }
        }
        return Optional.empty();
    }
}
