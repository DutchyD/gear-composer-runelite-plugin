package dev.dutchy.runelite.gear.ui;

import net.runelite.client.ui.ColorScheme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

/** A single-line field drawn as a rounded card, lit orange while it has focus. */
final class TextInput extends JTextField {

    TextInput() {
        setOpaque(false);
        setBackground(Ui.SURFACE);
        setForeground(ColorScheme.TEXT_COLOR);
        setCaretColor(ColorScheme.TEXT_COLOR);
        setSelectionColor(Highlights.selection(Ui.SURFACE));
        setBorder(BorderFactory.createEmptyBorder(5, 8, 5, 8));
        setPreferredSize(new Dimension(100, Ui.FIELD_HEIGHT));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, Ui.FIELD_HEIGHT));
        addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                repaint();
            }

            @Override
            public void focusLost(FocusEvent e) {
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), Ui.RADIUS, Ui.RADIUS);
            g2.setColor(outline());
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, Ui.RADIUS, Ui.RADIUS);
        } finally {
            g2.dispose();
        }
        super.paintComponent(g);
    }

    private Color outline() {
        return hasFocus() ? ColorScheme.BRAND_ORANGE : Ui.OUTLINE;
    }
}
