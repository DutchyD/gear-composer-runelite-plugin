package dev.dutchy.runelite.gear.ui;

import net.runelite.client.ui.ColorScheme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

/** A few lines of wrapped text drawn as a rounded card, like {@link TextInput}. */
final class TextArea extends JTextArea {

    TextArea(int rows) {
        super(rows, 10);
        setOpaque(false);
        setLineWrap(true);
        setWrapStyleWord(true);
        setBackground(Ui.SURFACE);
        setForeground(ColorScheme.TEXT_COLOR);
        setCaretColor(ColorScheme.TEXT_COLOR);
        setSelectionColor(Highlights.selection(Ui.SURFACE));
        setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
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
