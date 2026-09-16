package dev.dutchy.runelite.gear.bank;

import java.util.Objects;
import java.util.Set;

/**
 * Where each bank row of a layout starts: below whatever sits above the first row, and once rows
 * that carry dividers have grown a header strip above their items. Both sides share the same rows,
 * so they never drift apart.
 */
public final class BankRows {

    private static final int PITCH = BankGeometry.ITEM_HEIGHT + BankGeometry.ITEM_Y_PADDING;

    private final Set<Integer> headerRows;
    private final int inset;

    public BankRows(Set<Integer> headerRows) {
        this(headerRows, 0);
    }

    /** @param inset pixels taken above the first row, such as by the variant tabs */
    public BankRows(Set<Integer> headerRows, int inset) {
        this.headerRows = Set.copyOf(Objects.requireNonNull(headerRows, "headerRows"));
        if (inset < 0) {
            throw new IllegalArgumentException("Inset must not be negative, got " + inset);
        }
        this.inset = inset;
    }

    public static BankRows plain() {
        return new BankRows(Set.of());
    }

    public boolean hasHeader(int row) {
        return headerRows.contains(row);
    }

    /** The top of the row, where its header strip starts when it has one. */
    public int top(int row) {
        return inset + row * PITCH + headersBefore(row) * BankGeometry.HEADER_HEIGHT;
    }

    /** Where the row's items start: under its header strip when it has one. */
    public int itemY(int row) {
        return top(row) + (hasHeader(row) ? BankGeometry.HEADER_HEIGHT : 0);
    }

    /** The height the first {@code rows} rows take with what sits above them, without trailing padding. */
    public int height(int rows) {
        if (rows <= 0) {
            return inset;
        }
        return itemY(rows - 1) + BankGeometry.ITEM_HEIGHT;
    }

    private int headersBefore(int row) {
        return (int) headerRows.stream().filter(header -> header < row).count();
    }
}
