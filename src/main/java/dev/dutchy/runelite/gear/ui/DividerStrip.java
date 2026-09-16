package dev.dutchy.runelite.gear.ui;

import dev.dutchy.runelite.gear.content.*;
import dev.dutchy.runelite.gear.guide.HelpTopic;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * The dividers above one grid row, each drawn as a labelled rule across the columns it covers.
 * Click a divider to edit it, click a free stretch to start one there, right-click for quick changes.
 */
final class DividerStrip extends JPanel {

    static final int HEIGHT = 16;
    static final String EDIT = "Edit divider…";
    static final String ADD = "Add divider here…";
    static final String REMOVE = "Remove divider";

    private static final int GAP = 2;
    private static final Color FREE = new Color(70, 70, 70);

    private final SlotRef rowStart;
    private final List<Divider> dividers;
    private final SlotView.Listener listener;
    private int hoveredColumn = -1;

    DividerStrip(SlotRef rowStart, List<Divider> dividers, SlotView.Listener listener) {
        this.rowStart = Objects.requireNonNull(rowStart, "rowStart");
        this.dividers = List.copyOf(Objects.requireNonNull(dividers, "dividers"));
        this.listener = Objects.requireNonNull(listener, "listener");
        setBackground(ColorScheme.DARK_GRAY_COLOR);
        setPreferredSize(new Dimension(0, HEIGHT));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        Help.anchor(this, HelpTopic.DIVIDER);
        MouseAdapter mouse = new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                hoveredColumn = columnAt(e.getX());
                setToolTipText(dividerAt(hoveredColumn).map(divider -> divider.label() + " · click to edit, right-click for more")
                        .orElse("Click to add a divider over this column"));
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hoveredColumn = -1;
                repaint();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                maybeShowMenu(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                maybeShowMenu(e);
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    listener.editDivider(slotAt(columnAt(e.getX())));
                }
            }
        };
        addMouseListener(mouse);
        addMouseMotionListener(mouse);
    }

    List<String> labels() {
        return dividers.stream().map(Divider::label).collect(Collectors.toList());
    }

    /** The slot under the column, which is how the host names a divider's place. */
    SlotRef slotAt(int column) {
        return rowStart.gridSibling(rowStart.gridIndex() + column);
    }

    Optional<Divider> dividerAt(int column) {
        return dividers.stream().filter(divider -> divider.spansColumn(column)).findFirst();
    }

    ContextMenu contextMenu(int column) {
        SlotRef slot = slotAt(column);
        Optional<Divider> divider = dividerAt(column);
        if (divider.isEmpty()) {
            return new ContextMenu().item(ActionIcon.ADD, ADD, () -> listener.editDivider(slot));
        }
        ContextMenu menu = new ContextMenu().item(ActionIcon.RENAME, EDIT, () -> listener.editDivider(slot));
        for (DividerChange change : DividerChange.values()) {
            if (change.changes(divider.get())) {
                menu.item(ActionIcon.MOVE_DOWN, change.label(), () -> listener.changeDivider(slot, change));
            }
        }
        return menu.divider().danger(REMOVE, () -> listener.removeDivider(slot));
    }

    private int columnAt(int x) {
        return Math.max(0, Math.min(ItemGrid.COLUMNS - 1, x * ItemGrid.COLUMNS / Math.max(1, getWidth())));
    }

    private void maybeShowMenu(MouseEvent e) {
        if (e.isPopupTrigger()) {
            contextMenu(columnAt(e.getX())).show(this, e.getX(), e.getY());
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setFont(FontManager.getRunescapeSmallFont());
            FontMetrics metrics = g2.getFontMetrics();
            double column = (double) getWidth() / ItemGrid.COLUMNS;
            for (int c = 0; c < ItemGrid.COLUMNS; c++) {
                if (dividerAt(c).isEmpty()) {
                    g2.setColor(c == hoveredColumn ? ColorScheme.LIGHT_GRAY_COLOR : FREE);
                    g2.drawLine((int) (c * column) + GAP, getHeight() - 1, (int) ((c + 1) * column) - GAP, getHeight() - 1);
                }
            }
            for (Divider divider : dividers) {
                int left = (int) (divider.fromColumn() * column) + GAP;
                int right = (int) ((divider.toColumn() + 1) * column) - GAP;
                boolean lit = hoveredColumn >= 0 && divider.spansColumn(hoveredColumn);
                g2.setColor(lit ? ColorScheme.TEXT_COLOR : ColorScheme.BRAND_ORANGE);
                if (divider.underlined() || lit) {
                    g2.drawLine(left, getHeight() - 1, right, getHeight() - 1);
                }
                String text = fit(divider.label(), metrics, right - left);
                int x = alignedX(divider.align(), left + 1, right - 1, metrics.stringWidth(text));
                g2.drawString(text, x, getHeight() - 4);
            }
        } finally {
            g2.dispose();
        }
    }

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

    private static String fit(String text, FontMetrics metrics, int width) {
        if (metrics.stringWidth(text) <= width) {
            return text;
        }
        String shortened = text;
        while (shortened.length() > 1 && metrics.stringWidth(shortened + "…") > width) {
            shortened = shortened.substring(0, shortened.length() - 1);
        }
        return shortened + "…";
    }
}
