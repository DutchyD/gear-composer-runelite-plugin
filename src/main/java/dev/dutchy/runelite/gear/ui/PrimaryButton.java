package dev.dutchy.runelite.gear.ui;

import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;

/** The one call to action on a page: a filled brand-orange button. */
final class PrimaryButton extends JButton {

    private static final Color FILL = ColorScheme.BRAND_ORANGE;
    private static final Color FILL_HOVER = new Color(238, 156, 24);
    private static final Color FILL_PRESSED = new Color(190, 118, 0);
    private static final Color FILL_DISABLED = new Color(58, 58, 58);
    private static final Color TEXT = new Color(24, 20, 12);
    private static final Color TEXT_DISABLED = ColorScheme.MEDIUM_GRAY_COLOR;

    PrimaryButton(String text, String tooltip, Runnable action) {
        super(Objects.requireNonNull(text, "text"));
        Objects.requireNonNull(action, "action");
        setToolTipText(tooltip);
        setFont(FontManager.getRunescapeBoldFont());
        setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
        setPreferredSize(new Dimension(80, Ui.FIELD_HEIGHT));
        setFocusPainted(false);
        setContentAreaFilled(false);
        setOpaque(false);
        setRolloverEnabled(true);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addActionListener(e -> action.run());
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setColor(fill());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), Ui.RADIUS, Ui.RADIUS);
            g2.setFont(getFont());
            g2.setColor(isEnabled() ? TEXT : TEXT_DISABLED);
            FontMetrics metrics = g2.getFontMetrics();
            int x = (getWidth() - metrics.stringWidth(getText())) / 2;
            int y = (getHeight() - metrics.getHeight()) / 2 + metrics.getAscent();
            g2.drawString(getText(), x, y);
        } finally {
            g2.dispose();
        }
    }

    private Color fill() {
        if (!isEnabled()) {
            return FILL_DISABLED;
        }
        if (getModel().isPressed()) {
            return FILL_PRESSED;
        }
        return getModel().isRollover() ? FILL_HOVER : FILL;
    }
}
