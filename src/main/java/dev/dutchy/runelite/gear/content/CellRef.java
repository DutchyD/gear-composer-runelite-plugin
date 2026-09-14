package dev.dutchy.runelite.gear.content;

import lombok.Value;
import lombok.experimental.Accessors;

/** A position in a custom layout's grid: row from the top, column from the left. */
@Value
@Accessors(fluent = true)
public class CellRef {
    int row;
    int column;

    public CellRef(int row, int column) {
        if (row < 0 || row >= CustomContent.MAX_ROWS) {
            throw new IllegalArgumentException("Row must be within [0, " + CustomContent.MAX_ROWS + "), got " + row);
        }
        if (column < 0 || column >= CustomContent.COLUMNS) {
            throw new IllegalArgumentException("Column must be within [0, " + CustomContent.COLUMNS + "), got " + column);
        }
        this.row = row;
        this.column = column;
    }

    public static CellRef of(int row, int column) {
        return new CellRef(row, column);
    }

    public boolean isLeft() {
        return column == 0;
    }

    public String describe() {
        return "Row " + (row + 1) + " " + (isLeft() ? "left" : "right");
    }
}
