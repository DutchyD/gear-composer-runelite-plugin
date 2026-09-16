package dev.dutchy.runelite.gear.content;

import lombok.Value;

import java.util.Objects;

/** A labelled break above a grid row, spanning some or all of its columns, without taking a slot. */
@Value
public class Divider {
    int row;
    int fromColumn;
    int toColumn;
    String label;
    TextAlign align;
    boolean underlined;

    public static final int MAX_LABEL_LENGTH = 24;

    public Divider(int row, int fromColumn, int toColumn, String label, TextAlign align) {
        this(row, fromColumn, toColumn, label, align, true);
    }

    public Divider(int row, int fromColumn, int toColumn, String label, TextAlign align, boolean underlined) {
        if (row < 0 || row >= ItemGrid.ROWS) {
            throw new IllegalArgumentException("Row must be within [0, " + ItemGrid.ROWS + "), got " + row);
        }
        if (fromColumn < 0 || toColumn >= ItemGrid.COLUMNS || fromColumn > toColumn) {
            throw new IllegalArgumentException("Columns must run left to right within [0, " + ItemGrid.COLUMNS + "), got " + fromColumn + " to " + toColumn);
        }
        this.row = row;
        this.fromColumn = fromColumn;
        this.toColumn = toColumn;
        this.label = requireValidLabel(label);
        this.align = Objects.requireNonNull(align, "align");
        this.underlined = underlined;
    }

    /** A divider across the whole row, the way they always used to be. */
    public static Divider acrossRow(int row, String label) {
        return new Divider(row, 0, ItemGrid.COLUMNS - 1, label, TextAlign.DEFAULT);
    }

    public boolean spansColumn(int column) {
        return column >= fromColumn && column <= toColumn;
    }

    public boolean spansWholeRow() {
        return fromColumn == 0 && toColumn == ItemGrid.COLUMNS - 1;
    }

    public int width() {
        return toColumn - fromColumn + 1;
    }

    /** Whether the two sit above the same row and share a column. */
    public boolean overlaps(Divider other) {
        Objects.requireNonNull(other, "other");
        return row == other.row && fromColumn <= other.toColumn && other.fromColumn <= toColumn;
    }

    public Divider withLabel(String newLabel) {
        return new Divider(row, fromColumn, toColumn, newLabel, align, underlined);
    }

    public Divider withAlign(TextAlign newAlign) {
        return new Divider(row, fromColumn, toColumn, label, newAlign, underlined);
    }

    public Divider withUnderline(boolean isUnderlined) {
        return new Divider(row, fromColumn, toColumn, label, align, isUnderlined);
    }

    public Divider withColumns(int newFrom, int newTo) {
        return new Divider(row, newFrom, newTo, label, align, underlined);
    }

    public static boolean isValidLabel(String label) {
        return label != null && !label.isBlank() && label.strip().length() <= MAX_LABEL_LENGTH;
    }

    private static String requireValidLabel(String label) {
        if (!isValidLabel(label)) {
            throw new IllegalArgumentException("A divider label is 1 to " + MAX_LABEL_LENGTH + " characters");
        }
        return label.strip();
    }
}
