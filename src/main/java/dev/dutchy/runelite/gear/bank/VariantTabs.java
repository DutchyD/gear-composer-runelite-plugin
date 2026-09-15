package dev.dutchy.runelite.gear.bank;

import dev.dutchy.runelite.gear.GearSetup;
import dev.dutchy.runelite.gear.content.SetupVariant;
import lombok.Value;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * The row of variant tabs drawn above a layout in the bank: every variant's name with the chosen one
 * lit. Up to four share the strip's width; more than that scroll between a pair of arrows.
 */
@Value
public class VariantTabs {

    public static final VariantTabs NONE = new VariantTabs(List.of(), 0);
    public static final int VISIBLE = 4;
    public static final int GAP = 4;
    public static final int ARROW_WIDTH = 18;

    List<String> names;
    int chosen;

    public VariantTabs(List<String> names, int chosen) {
        this.names = List.copyOf(Objects.requireNonNull(names, "names"));
        if (!this.names.isEmpty() && (chosen < 0 || chosen >= this.names.size())) {
            throw new IllegalArgumentException("No tab " + chosen + " among " + this.names.size());
        }
        this.chosen = chosen;
    }

    /** Tabs for a setup with more than one variant, none otherwise. */
    public static VariantTabs of(GearSetup setup) {
        Objects.requireNonNull(setup, "setup");
        if (!setup.hasVariants()) {
            return NONE;
        }
        return new VariantTabs(setup.variants().stream().map(SetupVariant::name).collect(Collectors.toList()), setup.selectedIndex());
    }

    public boolean isEmpty() {
        return names.isEmpty();
    }

    public int count() {
        return names.size();
    }

    /** Room the strip takes above the first row, nothing when there are no tabs. */
    public int height() {
        return isEmpty() ? 0 : BankGeometry.VARIANT_STRIP_HEIGHT;
    }

    /** Whether there are more tabs than fit on show at once, so the arrows are needed. */
    public boolean overflows() {
        return count() > VISIBLE;
    }

    /** How many tabs are on show at once. */
    public int visibleCount() {
        return Math.min(count(), VISIBLE);
    }

    /** The first tab on show after scrolling by {@code offset}, kept within what can scroll. */
    public int clampOffset(int offset) {
        return Math.max(0, Math.min(offset, count() - visibleCount()));
    }

    /** The last offset that can be scrolled to. */
    public int maxOffset() {
        return clampOffset(Integer.MAX_VALUE);
    }

    /** The offset that brings the chosen tab into view from the given one. */
    public int offsetShowingChosen(int offset) {
        int first = clampOffset(offset);
        if (chosen < first) {
            return chosen;
        }
        int last = first + visibleCount() - 1;
        return chosen > last ? clampOffset(chosen - visibleCount() + 1) : first;
    }

    /** How wide each tab is when the visible ones share the strip's width, at least a pixel. */
    public int tabWidth(int width) {
        int room = width - (overflows() ? 2 * (ARROW_WIDTH + GAP) : 0) - (visibleCount() - 1) * GAP;
        return Math.max(1, room / visibleCount());
    }

    /** Where a visible tab starts, measured from the strip's left edge. */
    public int tabX(int position, int width) {
        return (overflows() ? ARROW_WIDTH + GAP : 0) + position * (tabWidth(width) + GAP);
    }
}
