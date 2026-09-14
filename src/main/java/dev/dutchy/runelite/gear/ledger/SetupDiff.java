package dev.dutchy.runelite.gear.ledger;

import dev.dutchy.runelite.gear.content.*;
import dev.dutchy.runelite.gear.layout.Layout;
import dev.dutchy.runelite.gear.layout.LayoutBlock;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Value;
import lombok.experimental.Accessors;

import java.util.*;
import java.util.stream.Collectors;

/** Slot-by-slot changes between two contents of the same shape. */
@Value
@Accessors(fluent = true)
public class SetupDiff {
    List<SlotChange> changes;

    public SetupDiff(List<SlotChange> changes) {
        this.changes = List.copyOf(Objects.requireNonNull(changes, "changes"));
    }

    /** One slot that differs; either side may be empty. */
    @Value
    @Accessors(fluent = true)
    public static class SlotChange {
        SlotRef ref;
        @Getter(AccessLevel.NONE)
        SetupItem before;
        @Getter(AccessLevel.NONE)
        SetupItem after;

        public SlotChange(SlotRef ref, SetupItem before, SetupItem after) {
            this.ref = Objects.requireNonNull(ref, "ref");
            this.before = before;
            this.after = after;
        }

        public Optional<SetupItem> before() {
            return Optional.ofNullable(before);
        }

        public Optional<SetupItem> after() {
            return Optional.ofNullable(after);
        }

        public boolean isSameItem() {
            return before != null && after != null && before.id().equals(after.id());
        }

        /** Change in a stated quantity when both sides name the same item, else empty. */
        public OptionalInt quantityDelta() {
            if (!isSameItem() || !before.hasQuantity() || !after.hasQuantity()) {
                return OptionalInt.empty();
            }
            return OptionalInt.of(after.quantity().orElse(0) - before.quantity().orElse(0));
        }
    }

    /** Same shape: the same type, and for custom layouts the same number of rows. */
    public static boolean comparable(SetupContent left, SetupContent right) {
        if (left.type() != right.type()) {
            return false;
        }
        return !(left instanceof CustomContent) || ((CustomContent) left).rows() == ((CustomContent) right).rows();
    }

    public static SetupDiff between(SetupContent before, SetupContent after) {
        Objects.requireNonNull(before, "before");
        Objects.requireNonNull(after, "after");
        if (!comparable(before, after)) {
            throw new IllegalArgumentException("Only setups of the same type can be compared");
        }
        List<SlotChange> changes = new ArrayList<>();
        for (SlotRef ref : slotsOf(before)) {
            SetupItem was = SetupContentEditor.itemAt(before, ref).orElse(null);
            SetupItem now = SetupContentEditor.itemAt(after, ref).orElse(null);
            if (!Objects.equals(was, now)) {
                changes.add(new SlotChange(ref, was, now));
            }
        }
        return new SetupDiff(changes);
    }

    public boolean isEmpty() {
        return changes.isEmpty();
    }

    private static List<SlotRef> slotsOf(SetupContent content) {
        return Layout.of(content).slots().stream().map(LayoutBlock.Slot::ref).collect(Collectors.toList());
    }
}
