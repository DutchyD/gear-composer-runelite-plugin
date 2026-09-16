package dev.dutchy.runelite.gear.layout;

import dev.dutchy.runelite.gear.content.ItemGrid;
import dev.dutchy.runelite.gear.content.TextAlign;
import lombok.Value;

import java.util.Objects;

/**
 * Text the bank paints for a layout: a band's title on a blank row across its side, or a divider in
 * the header strip of the row it heads, across the columns it spans.
 */
@Value
public class LayoutLabel {
    BankSide side;
    int row;
    int fromColumn;
    int toColumn;
    String text;
    TextAlign align;
    boolean header;
    boolean underlined;

    /** A title on a blank row across the whole side, read from the left. */
    public LayoutLabel(BankSide side, int row, String text) {
        this(side, row, 0, ItemGrid.COLUMNS - 1, text, TextAlign.LEFT, false, false);
    }

    public LayoutLabel(BankSide side, int row, int fromColumn, int toColumn, String text, TextAlign align, boolean header, boolean underlined) {
        this.side = Objects.requireNonNull(side, "side");
        if (row < 0) {
            throw new IllegalArgumentException("Row must not be negative, got " + row);
        }
        if (fromColumn < 0 || toColumn >= ItemGrid.COLUMNS || fromColumn > toColumn) {
            throw new IllegalArgumentException("Columns must run left to right within a side, got " + fromColumn + " to " + toColumn);
        }
        this.row = row;
        this.fromColumn = fromColumn;
        this.toColumn = toColumn;
        this.text = Objects.requireNonNull(text, "text");
        this.align = Objects.requireNonNull(align, "align");
        this.header = header;
        this.underlined = underlined;
    }

    /** A divider in the header strip of the row it heads. */
    public static LayoutLabel header(BankSide side, int row, int fromColumn, int toColumn, String text, TextAlign align, boolean underlined) {
        return new LayoutLabel(side, row, fromColumn, toColumn, text, align, true, underlined);
    }

    public int width() {
        return toColumn - fromColumn + 1;
    }

}
