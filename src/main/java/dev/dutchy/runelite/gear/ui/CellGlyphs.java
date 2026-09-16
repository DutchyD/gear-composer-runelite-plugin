package dev.dutchy.runelite.gear.ui;

import dev.dutchy.runelite.gear.content.CellKind;
import dev.dutchy.runelite.gear.content.EquipmentSlot;
import dev.dutchy.runelite.gear.content.ItemGrid;

import java.awt.*;

/** Small drawings of a cell kind: a cross for equipment, a grid for an inventory, a dashed box for nothing. */
final class CellGlyphs {

    private CellGlyphs() {
    }

    static void paint(Graphics2D g, CellKind kind, int x, int y, int width, int height) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        switch (kind) {
            case EQUIPMENT:
                cross(g, x, y, width, height);
                break;
            case INVENTORY:
                grid(g, x, y, width, height);
                break;
            default:
                g.setStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1f, new float[]{2f, 2f}, 0f));
                g.drawRect(x, y, width - 1, height - 1);
                break;
        }
    }

    private static void cross(Graphics2D g, int x, int y, int width, int height) {
        float cellWidth = (float) width / EquipmentSlot.COLUMNS;
        float cellHeight = (float) height / EquipmentSlot.ROWS;
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            int left = Math.round(x + slot.column() * cellWidth);
            int top = Math.round(y + slot.row() * cellHeight);
            g.fillRect(left, top, Math.max(1, Math.round(cellWidth) - 1), Math.max(1, Math.round(cellHeight) - 1));
        }
    }

    private static void grid(Graphics2D g, int x, int y, int width, int height) {
        float cellWidth = (float) width / ItemGrid.COLUMNS;
        float cellHeight = (float) height / ItemGrid.ROWS;
        for (int row = 0; row < ItemGrid.ROWS; row++) {
            for (int column = 0; column < ItemGrid.COLUMNS; column++) {
                int left = Math.round(x + column * cellWidth);
                int top = Math.round(y + row * cellHeight);
                g.fillRect(left, top, Math.max(1, Math.round(cellWidth) - 1), Math.max(1, Math.round(cellHeight) - 1));
            }
        }
    }
}
