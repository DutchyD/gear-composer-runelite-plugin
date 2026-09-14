package dev.dutchy.runelite.gear.ui;

import dev.dutchy.runelite.gear.content.CellRef;
import dev.dutchy.runelite.gear.content.CustomContent;
import dev.dutchy.runelite.gear.content.LayoutCell;
import net.runelite.client.ui.ColorScheme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

/** The layout in its real shape, small enough for the header; clicking a cell jumps to it. */
final class LayoutMap extends JPanel {

    static final int TILE = 30;
    private static final int GAP = 3;

    private final Map<CellRef, Tile> tiles = new HashMap<>();
    private CellRef current;

    LayoutMap(CustomContent content, Consumer<CellRef> onPick) {
        Objects.requireNonNull(content, "content");
        Objects.requireNonNull(onPick, "onPick");
        setLayout(new GridLayout(content.rows(), CustomContent.COLUMNS, GAP, GAP));
        setBackground(ColorScheme.DARK_GRAY_COLOR);
        for (CellRef ref : content.cellRefs()) {
            Tile tile = new Tile(ref, content.cell(ref), () -> onPick.accept(ref));
            tiles.put(ref, tile);
            add(tile);
        }
    }

    CellRef current() {
        return current;
    }

    void setCurrent(CellRef ref) {
        current = ref;
        tiles.forEach((position, tile) -> tile.setCurrent(position.equals(ref)));
    }

    private static final class Tile extends Card {
        private final LayoutCell cell;
        private boolean current;
        private boolean hovered;

        Tile(CellRef ref, LayoutCell cell, Runnable onClick) {
            this.cell = cell;
            setPreferredSize(new Dimension(TILE, TILE));
            setRadius(4);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setToolTipText(ref.describe() + " · " + cell.label());
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    onClick.run();
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    hovered = true;
                    refresh();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hovered = false;
                    refresh();
                }
            });
            refresh();
        }

        void setCurrent(boolean isCurrent) {
            current = isCurrent;
            refresh();
        }

        private void refresh() {
            setBackground(current ? Highlights.selection(Ui.SURFACE) : hovered ? Ui.SURFACE_HOVER : Ui.SURFACE);
            setOutline(current ? ColorScheme.BRAND_ORANGE : cell.isBlank() ? ColorScheme.BORDER_COLOR : Ui.OUTLINE);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                g2.setColor(cell.isEmpty() ? ColorScheme.MEDIUM_GRAY_COLOR : ColorScheme.TEXT_COLOR);
                CellGlyphs.paint(g2, cell.kind(), 6, 6, getWidth() - 12, getHeight() - 12);
            } finally {
                g2.dispose();
            }
        }
    }
}
