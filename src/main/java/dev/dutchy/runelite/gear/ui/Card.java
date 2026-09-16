package dev.dutchy.runelite.gear.ui;

import net.runelite.client.ui.ColorScheme;

import javax.swing.*;
import java.awt.*;

/** A rounded, outlined surface that stays opaque so repeated repaints never stack its antialiased edge. */
class Card extends JPanel {

    private Color outline = Ui.OUTLINE;
    private int radius = Ui.RADIUS;

    Card() {
        setOpaque(true);
        setBackground(Ui.SURFACE);
    }

    void setOutline(Color color) {
        outline = color;
        repaint();
    }

    void setRadius(int newRadius) {
        radius = newRadius;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setColor(backdrop());
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
            if (outline != null) {
                g2.setColor(outline);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
            }
        } finally {
            g2.dispose();
        }
    }

    /** Colour showing through the rounded corners: whatever opaque surface this card sits on. */
    private Color backdrop() {
        for (Container parent = getParent(); parent != null; parent = parent.getParent()) {
            if (parent.isOpaque()) {
                return parent.getBackground();
            }
        }
        return ColorScheme.DARK_GRAY_COLOR;
    }
}
