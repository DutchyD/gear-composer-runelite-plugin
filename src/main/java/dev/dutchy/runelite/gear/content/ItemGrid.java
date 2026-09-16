package dev.dutchy.runelite.gear.content;

import java.util.*;
import java.util.stream.Collectors;

/** Four by seven slots, plus labelled dividers that head a row without taking a slot. */
public final class ItemGrid {

    public static final int COLUMNS = 4;
    public static final int ROWS = 7;
    public static final int SIZE = COLUMNS * ROWS;
    public static final int MAX_DIVIDER_LENGTH = Divider.MAX_LABEL_LENGTH;
    public static final ItemGrid EMPTY = new ItemGrid(Map.of(), List.of());

    private static final Comparator<Divider> READING_ORDER = Comparator.comparingInt(Divider::row).thenComparingInt(Divider::fromColumn);

    private final Map<Integer, SetupItem> slots;
    private final List<Divider> dividers;

    private ItemGrid(Map<Integer, SetupItem> slots, List<Divider> dividers) {
        this.slots = Collections.unmodifiableMap(new LinkedHashMap<>(slots));
        List<Divider> ordered = new ArrayList<>(dividers);
        ordered.sort(READING_ORDER);
        this.dividers = List.copyOf(ordered);
    }

    public static ItemGrid of(Map<Integer, SetupItem> slots) {
        return of(slots, List.of());
    }

    public static ItemGrid of(Map<Integer, SetupItem> slots, List<Divider> dividers) {
        Objects.requireNonNull(slots, "slots");
        Objects.requireNonNull(dividers, "dividers");
        slots.keySet().forEach(ItemGrid::requireValidIndex);
        for (int i = 0; i < dividers.size(); i++) {
            for (int j = 0; j < i; j++) {
                if (dividers.get(i).overlaps(dividers.get(j))) {
                    throw new IllegalArgumentException("Dividers " + dividers.get(j).label() + " and " + dividers.get(i).label() + " share a column");
                }
            }
        }
        return new ItemGrid(slots, dividers);
    }

    public Optional<SetupItem> slot(int index) {
        requireValidIndex(index);
        return Optional.ofNullable(slots.get(index));
    }

    public ItemGrid withSlot(int index, SetupItem item) {
        requireValidIndex(index);
        Objects.requireNonNull(item, "item");
        Map<Integer, SetupItem> updated = new LinkedHashMap<>(slots);
        updated.put(index, item);
        return new ItemGrid(updated, dividers);
    }

    public ItemGrid clearSlot(int index) {
        requireValidIndex(index);
        if (!slots.containsKey(index)) {
            return this;
        }
        Map<Integer, SetupItem> updated = new LinkedHashMap<>(slots);
        updated.remove(index);
        return new ItemGrid(updated, dividers);
    }

    /** Every divider, top to bottom and left to right. */
    public List<Divider> dividers() {
        return dividers;
    }

    public List<Divider> dividersAbove(int row) {
        requireValidRow(row);
        return dividers.stream().filter(divider -> divider.row() == row).collect(Collectors.toList());
    }

    public boolean hasDividerAbove(int row) {
        return !dividersAbove(row).isEmpty();
    }

    /** The divider over the column of the row, if one spans it. */
    public Optional<Divider> dividerAt(int row, int column) {
        return dividersAbove(row).stream().filter(divider -> divider.spansColumn(column)).findFirst();
    }

    /** Puts the divider in place; whatever it overlaps is trimmed back to the columns it leaves free, or dropped. */
    public ItemGrid withDivider(Divider divider) {
        Objects.requireNonNull(divider, "divider");
        List<Divider> updated = new ArrayList<>();
        for (Divider existing : dividers) {
            if (!existing.overlaps(divider)) {
                updated.add(existing);
            } else if (existing.fromColumn() < divider.fromColumn()) {
                updated.add(existing.withColumns(existing.fromColumn(), divider.fromColumn() - 1));
            } else if (existing.toColumn() > divider.toColumn()) {
                updated.add(existing.withColumns(divider.toColumn() + 1, existing.toColumn()));
            }
        }
        updated.add(divider);
        return new ItemGrid(slots, updated);
    }

    /** A divider across the whole row. */
    public ItemGrid withDivider(int row, String label) {
        return withDivider(Divider.acrossRow(row, label));
    }

    public ItemGrid withoutDivider(Divider divider) {
        Objects.requireNonNull(divider, "divider");
        if (!dividers.contains(divider)) {
            return this;
        }
        List<Divider> updated = new ArrayList<>(dividers);
        updated.remove(divider);
        return new ItemGrid(slots, updated);
    }

    /** Drops every divider above the row. */
    public ItemGrid withoutDividersAbove(int row) {
        requireValidRow(row);
        if (!hasDividerAbove(row)) {
            return this;
        }
        return new ItemGrid(slots, dividers.stream().filter(divider -> divider.row() != row).collect(Collectors.toList()));
    }

    public boolean isEmpty() {
        return slots.isEmpty();
    }

    public int filledSlots() {
        return slots.size();
    }

    /** The rows that carry dividers and so grow a header strip, top to bottom. */
    public SortedSet<Integer> rowsWithDividers() {
        return dividers.stream().map(Divider::row).collect(Collectors.toCollection(TreeSet::new));
    }

    public static int column(int index) {
        requireValidIndex(index);
        return index % COLUMNS;
    }

    public static int row(int index) {
        requireValidIndex(index);
        return index / COLUMNS;
    }

    private static void requireValidRow(int row) {
        if (row < 0 || row >= ROWS) {
            throw new IllegalArgumentException("Row must be within [0, " + ROWS + "), got " + row);
        }
    }

    private static void requireValidIndex(int index) {
        if (index < 0 || index >= SIZE) {
            throw new IllegalArgumentException("Slot index must be within [0, " + SIZE + "), got " + index);
        }
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof ItemGrid && slots.equals(((ItemGrid) other).slots) && dividers.equals(((ItemGrid) other).dividers);
    }

    @Override
    public int hashCode() {
        return Objects.hash(slots, dividers);
    }

    @Override
    public String toString() {
        return "ItemGrid[" + slots.size() + " of " + SIZE + " filled, " + dividers.size() + " divider(s)]";
    }
}
