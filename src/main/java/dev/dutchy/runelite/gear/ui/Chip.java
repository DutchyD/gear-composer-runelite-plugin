package dev.dutchy.runelite.gear.ui;

import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;

/** A small pill that fills in a value and lights up while the field holds that value. */
final class Chip extends JButton {

    private static final int HEIGHT = 22;

    private boolean chosen;

    /** A chip as wide as its text, for flowing rows rather than grids. */
    static Chip sized(String text, String tooltip, Runnable action) {
        Chip chip = new Chip(text, tooltip, action);
        int width = chip.getFontMetrics(chip.getFont()).stringWidth(text) + 18;
        chip.setPreferredSize(new Dimension(width, HEIGHT));
        return chip;
    }

    Chip(String text, String tooltip, Runnable action) {
        super(Objects.requireNonNull(text, "text"));
        Objects.requireNonNull(action, "action");
        setToolTipText(tooltip);
        setFont(FontManager.getRunescapeSmallFont());
        setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
        setPreferredSize(new Dimension(40, HEIGHT));
        setMinimumSize(new Dimension(20, HEIGHT));
        setFocusPainted(false);
        setContentAreaFilled(false);
        setOpaque(false);
        setRolloverEnabled(true);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addActionListener(e -> action.run());
    }

    boolean isChosen() {
        return chosen;
    }

    void setChosen(boolean isChosen) {
        chosen = isChosen;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setColor(fill());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), Ui.RADIUS, Ui.RADIUS);
            g2.setColor(chosen ? ColorScheme.BRAND_ORANGE : Ui.OUTLINE);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, Ui.RADIUS, Ui.RADIUS);
            g2.setFont(getFont());
            g2.setColor(chosen ? ColorScheme.TEXT_COLOR : ColorScheme.LIGHT_GRAY_COLOR);
            FontMetrics metrics = g2.getFontMetrics();
            int x = (getWidth() - metrics.stringWidth(getText())) / 2;
            int y = (getHeight() - metrics.getHeight()) / 2 + metrics.getAscent();
            g2.drawString(getText(), x, y);
        } finally {
            g2.dispose();
        }
    }

    private Color fill() {
        if (chosen) {
            return Highlights.selection(Ui.SURFACE);
        }
        return getModel().isRollover() ? Ui.SURFACE_HOVER : Ui.SURFACE;
    }
}
