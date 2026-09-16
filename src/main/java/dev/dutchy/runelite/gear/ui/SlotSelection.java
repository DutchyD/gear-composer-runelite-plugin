package dev.dutchy.runelite.gear.ui;

import dev.dutchy.runelite.gear.content.SlotRef;

import java.util.*;

/** Which slots on the contents page are selected; click, shift-click for a range, ctrl-click to toggle. */
final class SlotSelection {

    private final Set<SlotRef> selected = new LinkedHashSet<>();
    private SlotRef anchor;

    void click(SlotRef ref, boolean shift, boolean control, List<SlotRef> order) {
        Objects.requireNonNull(ref, "ref");
        Objects.requireNonNull(order, "order");
        if (shift && anchor != null) {
            int from = order.indexOf(anchor);
            int to = order.indexOf(ref);
            if (from >= 0 && to >= 0) {
                if (!control) {
                    selected.clear();
                }
                for (int index = Math.min(from, to); index <= Math.max(from, to); index++) {
                    selected.add(order.get(index));
                }
                return;
            }
        }
        if (control) {
            if (!selected.remove(ref)) {
                selected.add(ref);
            }
        } else {
            selected.clear();
            selected.add(ref);
        }
        anchor = ref;
    }

    void clear() {
        selected.clear();
        anchor = null;
    }

    boolean contains(SlotRef ref) {
        return selected.contains(ref);
    }

    boolean isEmpty() {
        return selected.isEmpty();
    }

    int size() {
        return selected.size();
    }

    /** Selected slots in page order. */
    List<SlotRef> ordered(List<SlotRef> order) {
        List<SlotRef> ordered = new ArrayList<>();
        for (SlotRef ref : order) {
            if (selected.contains(ref)) {
                ordered.add(ref);
            }
        }
        return ordered;
    }

    Optional<SlotRef> anchor() {
        return Optional.ofNullable(anchor);
    }
}
