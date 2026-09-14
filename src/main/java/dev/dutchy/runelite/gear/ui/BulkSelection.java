package dev.dutchy.runelite.gear.ui;

import dev.dutchy.runelite.gear.SectionId;
import dev.dutchy.runelite.gear.SetupId;

import java.util.*;
import java.util.stream.Collectors;

final class BulkSelection {

    private final Set<BulkTarget> selected = new LinkedHashSet<>();
    private BulkTarget anchor;

    /**
     * A plain click toggles one target and becomes the anchor. A shift click adds everything between
     * the anchor and the target, in the order given, leaving the anchor where it was.
     */
    void click(BulkTarget target, boolean shiftDown, List<BulkTarget> order) {
        Objects.requireNonNull(target, "target");
        Objects.requireNonNull(order, "order");
        if (shiftDown && anchor != null) {
            int from = order.indexOf(anchor);
            int to = order.indexOf(target);
            if (from >= 0 && to >= 0) {
                for (int i = Math.min(from, to); i <= Math.max(from, to); i++) {
                    selected.add(order.get(i));
                }
                return;
            }
        }
        if (!selected.remove(target)) {
            selected.add(target);
        }
        anchor = target;
    }

    boolean isSelected(BulkTarget target) {
        return selected.contains(target);
    }

    int size() {
        return selected.size();
    }

    boolean isEmpty() {
        return selected.isEmpty();
    }

    void clear() {
        selected.clear();
        anchor = null;
    }

    /** Drops targets that no longer exist, so a deleted item cannot linger in the selection. */
    void retainAll(Collection<BulkTarget> valid) {
        selected.retainAll(Set.copyOf(valid));
        if (anchor != null && !valid.contains(anchor)) {
            anchor = null;
        }
    }

    Set<SectionId> sections() {
        return selected.stream()
                .filter(BulkTarget.Section.class::isInstance)
                .map(target -> ((BulkTarget.Section) target).id())
                .collect(Collectors.toUnmodifiableSet());
    }

    Set<SetupId> setups() {
        return selected.stream()
                .filter(BulkTarget.Setup.class::isInstance)
                .map(target -> ((BulkTarget.Setup) target).id())
                .collect(Collectors.toUnmodifiableSet());
    }
}
