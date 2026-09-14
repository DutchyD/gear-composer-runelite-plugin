package dev.dutchy.runelite.gear.share;

import dev.dutchy.runelite.gear.GearSetup;
import dev.dutchy.runelite.gear.content.*;
import dev.dutchy.runelite.gear.layout.*;
import dev.dutchy.runelite.gear.ledger.Ledger;
import dev.dutchy.runelite.gear.ledger.LedgerFormat;
import dev.dutchy.runelite.gear.ledger.LedgerRows;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Objects;
import java.util.SortedSet;

/** Paints a setup the way the sidebar shows it, band by band, at a size that reads well in a chat. */
public final class SetupImageRenderer {

    static final int CELL = 40;
    static final int SPRITE_WIDTH = 36;
    static final int SPRITE_HEIGHT = 32;
    static final int MARGIN = 14;
    static final int GAP = 10;
    private static final int LINE = 18;
    static final int HEADER = 16;
    private static final int TILE_HEIGHT = 40;
    private static final int TABLE_ROW = 30;
    private static final int TABLE_PADDING = 4;
    private static final Color BACKGROUND = ColorScheme.DARKER_GRAY_COLOR;
    private static final Color CELL_FILL = new Color(40, 40, 40);
    private static final Color CELL_EDGE = new Color(58, 58, 58);

    private SetupImageRenderer() {
    }

    /** {@code ledger} is the footer of totals, null to leave it off. */
    public static BufferedImage render(GearSetup setup, LoadedSetup loaded, Ledger ledger) {
        Objects.requireNonNull(setup, "setup");
        Objects.requireNonNull(loaded, "loaded");
        Layout layout = Layout.of(setup.content());
        int leftColumns = columnsOn(layout, BankSide.LEFT);
        int rightColumns = columnsOn(layout, BankSide.RIGHT);
        int width = 2 * MARGIN + (leftColumns + rightColumns) * CELL + GAP;
        int height = MARGIN + LINE + GAP + bodyHeight(layout)
                + (ledger == null ? 0 : ledgerHeight(ledger)) + MARGIN;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g.setColor(BACKGROUND);
            g.fillRect(0, 0, width, height);
            int y = MARGIN;
            g.setFont(FontManager.getRunescapeBoldFont());
            g.setColor(ColorScheme.BRAND_ORANGE);
            g.drawString(setup.name(), MARGIN, y + 14);
            g.setFont(FontManager.getRunescapeSmallFont());
            g.setColor(ColorScheme.LIGHT_GRAY_COLOR);
            g.drawString(setup.type().displayName(), width - MARGIN - g.getFontMetrics().stringWidth(setup.type().displayName()), y + 14);
            y += LINE + GAP;
            int rightX = MARGIN + leftColumns * CELL + GAP;
            for (Band band : layout.bands()) {
                if (band.labelRow().isPresent()) {
                    g.setFont(FontManager.getRunescapeSmallFont());
                    g.setColor(ColorScheme.BRAND_ORANGE);
                    for (LayoutBlock block : band.blocks()) {
                        g.drawString(block.title(), block.side() == BankSide.LEFT ? MARGIN : rightX, y + 12);
                    }
                    y += LINE;
                }
                SortedSet<Integer> headers = band.headerRows();
                for (LayoutBlock block : band.blocks()) {
                    paintBlock(g, block, loaded, block.side() == BankSide.LEFT ? MARGIN : rightX, y, headers);
                }
                y += band.rowsTall() * CELL + headers.size() * HEADER + GAP;
            }
            if (ledger != null) {
                paintLedger(g, ledger, y, width - 2 * MARGIN);
            }
        } finally {
            g.dispose();
        }
        return image;
    }

    private static int columnsOn(Layout layout, BankSide side) {
        return layout.blocks().stream().filter(block -> block.side() == side)
                .mapToInt(LayoutBlock::columnsWide).max().orElse(ItemGrid.COLUMNS);
    }

    private static int bodyHeight(Layout layout) {
        int height = 0;
        for (Band band : layout.bands()) {
            height += (band.labelRow().isPresent() ? LINE : 0) + band.rowsTall() * CELL + band.headerRows().size() * HEADER + GAP;
        }
        return Math.max(height, LINE);
    }

    /** The top of a row's items within a band whose rows grow a header where they carry dividers. */
    private static int rowY(int y, int row, SortedSet<Integer> headers) {
        return y + row * CELL + headers.headSet(row + 1).size() * HEADER;
    }

    private static void paintBlock(Graphics2D g, LayoutBlock block, LoadedSetup loaded, int x, int y, SortedSet<Integer> headers) {
        block.accept(new LayoutBlock.Visitor<Void>() {
            @Override
            public Void equipment(EquipmentBlock equipment) {
                for (EquipmentSlot slot : EquipmentSlot.values()) {
                    cell(g, equipment.equipment().get(slot), loaded, x + slot.column() * CELL, rowY(y, slot.row(), headers));
                }
                return null;
            }

            @Override
            public Void grid(GridBlock grid) {
                ItemGrid items = grid.grid();
                for (int index = 0; index < ItemGrid.SIZE; index++) {
                    cell(g, items.slot(index).orElse(null), loaded, x + ItemGrid.column(index) * CELL, rowY(y, ItemGrid.row(index), headers));
                }
                g.setFont(FontManager.getRunescapeSmallFont());
                for (Divider divider : items.dividers()) {
                    int top = rowY(y, divider.row(), headers) - HEADER;
                    int left = x + divider.fromColumn() * CELL + 2;
                    int right = x + (divider.toColumn() + 1) * CELL - 3;
                    g.setColor(ColorScheme.BRAND_ORANGE);
                    g.drawString(divider.label(), alignedX(divider.align(), left + 1, right - 1, g.getFontMetrics().stringWidth(divider.label())), top + HEADER - 4);
                    if (divider.underlined()) {
                        g.drawLine(left, top + HEADER - 1, right, top + HEADER - 1);
                    }
                }
                return null;
            }
        });
    }

    /** Where text of the given width starts so it sits left, centred or right between the edges. */
    static int alignedX(TextAlign align, int left, int right, int textWidth) {
        switch (align) {
            case CENTRE:
                return left + (right - left - textWidth) / 2;
            case RIGHT:
                return right - textWidth;
            default:
                return left;
        }
    }

    /** {@code item} is what the slot holds, null when it is empty. */
    private static void cell(Graphics2D g, SetupItem item, LoadedSetup loaded, int x, int y) {
        g.setColor(CELL_FILL);
        g.fillRect(x + 1, y + 1, CELL - 2, CELL - 2);
        g.setColor(CELL_EDGE);
        g.drawRect(x + 1, y + 1, CELL - 3, CELL - 3);
        if (item == null) {
            return;
        }
        loaded.sprite(item.id()).ifPresent(sprite ->
                g.drawImage(sprite, x + (CELL - SPRITE_WIDTH) / 2, y + (CELL - SPRITE_HEIGHT) / 2, SPRITE_WIDTH, SPRITE_HEIGHT, null));
        if (item.showsQuantity()) {
            int amount = item.quantity().orElse(1);
            String text = ItemQuantityFormat.text(amount);
            g.setFont(FontManager.getRunescapeSmallFont());
            g.setColor(Color.BLACK);
            g.drawString(text, x + 4, y + 13);
            g.setColor(ItemQuantityFormat.color(amount));
            g.drawString(text, x + 3, y + 12);
        }
    }

    /** Two figure tiles and, when anything is worn, the bonus table the equipment screen shows. */
    static int ledgerHeight(Ledger ledger) {
        int height = TILE_HEIGHT + GAP;
        if (!ledger.stats().isZero()) {
            height += LedgerRows.of(ledger.stats()).size() * TABLE_ROW + TABLE_PADDING * 2 + GAP;
        }
        return height;
    }

    private static void paintLedger(Graphics2D g, Ledger ledger, int y, int width) {
        int x = MARGIN;
        int tileWidth = (width - GAP) / 2;
        String value = LedgerFormat.coins(ledger.value()) + (ledger.openEnded() ? " +bank" : "");
        tile(g, "Value", value, x, y, tileWidth);
        tile(g, "Weight", LedgerFormat.weight(ledger.weight()), x + tileWidth + GAP, y, tileWidth);
        if (ledger.stats().isZero()) {
            return;
        }
        int top = y + TILE_HEIGHT + GAP;
        List<LedgerRows.Row> rows = LedgerRows.of(ledger.stats());
        int height = rows.size() * TABLE_ROW + TABLE_PADDING * 2;
        box(g, x, top, width, height);
        int labelWidth = 50;
        int cellWidth = (width - labelWidth - 2 * TABLE_PADDING) / LedgerRows.STYLES.size();
        int rowY = top + TABLE_PADDING;
        for (LedgerRows.Row row : rows) {
            g.setFont(FontManager.getRunescapeSmallFont());
            g.setColor(ColorScheme.LIGHT_GRAY_COLOR);
            g.drawString(row.label(), x + TABLE_PADDING, rowY + 22);
            int cellX = x + TABLE_PADDING + labelWidth;
            for (LedgerRows.Cell cell : row.cells()) {
                centred(g, cell.shortCaption(), cellX, rowY + 11, cellWidth, ColorScheme.MEDIUM_GRAY_COLOR);
                centred(g, cell.text(), cellX, rowY + 25, cellWidth, toneColour(cell.tone()));
                cellX += cellWidth;
            }
            rowY += TABLE_ROW;
        }
    }

    private static void tile(Graphics2D g, String caption, String figure, int x, int y, int width) {
        box(g, x, y, width, TILE_HEIGHT);
        g.setFont(FontManager.getRunescapeSmallFont());
        g.setColor(ColorScheme.LIGHT_GRAY_COLOR);
        g.drawString(caption, x + 8, y + 14);
        g.setFont(FontManager.getRunescapeBoldFont());
        g.setColor(ColorScheme.BRAND_ORANGE);
        g.drawString(figure, x + 8, y + 32);
    }

    private static void box(Graphics2D g, int x, int y, int width, int height) {
        g.setColor(CELL_FILL);
        g.fillRoundRect(x, y, width, height, 6, 6);
        g.setColor(CELL_EDGE);
        g.drawRoundRect(x, y, width - 1, height - 1, 6, 6);
    }

    private static void centred(Graphics2D g, String text, int x, int baseline, int width, Color colour) {
        g.setColor(colour);
        g.drawString(text, x + (width - g.getFontMetrics().stringWidth(text)) / 2, baseline);
    }

    private static Color toneColour(LedgerRows.Tone tone) {
        switch (tone) {
            case UP:
                return new Color(120, 220, 140);
            case DOWN:
                return new Color(220, 62, 48);
            default:
                return ColorScheme.MEDIUM_GRAY_COLOR;
        }
    }
}
