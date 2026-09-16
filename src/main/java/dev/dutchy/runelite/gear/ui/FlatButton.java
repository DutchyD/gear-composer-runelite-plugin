package dev.dutchy.runelite.gear.ui;

import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;

/** A quiet, borderless button that only lights up under the cursor. */
class FlatButton extends JButton {

    private static final Color TEXT = ColorScheme.LIGHT_GRAY_COLOR;
    private static final Color TEXT_HOVER = ColorScheme.BRAND_ORANGE;
    private static final Color TEXT_DISABLED = ColorScheme.MEDIUM_GRAY_COLOR;

    private boolean outlined;

    /** A button with a visible edge, for actions that should be found without hovering. */
    static FlatButton outlined(Icon icon, String text, String tooltip, Runnable action) {
        FlatButton button = new FlatButton(icon, text, tooltip, action);
        button.outlined = true;
        button.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        button.setIconTextGap(4);
        return button;
    }

    FlatButton(String text, String tooltip, Runnable action) {
        super(Objects.requireNonNull(text, "text"));
        init(tooltip, action, 8);
    }

    FlatButton(Icon icon, String tooltip, Runnable action) {
        super(Objects.requireNonNull(icon, "icon"));
        init(tooltip, action, 4);
    }

    FlatButton(Icon icon, String text, String tooltip, Runnable action) {
        super(Objects.requireNonNull(text, "text"), Objects.requireNonNull(icon, "icon"));
        setIconTextGap(5);
        init(tooltip, action, 8);
    }

    private void init(String tooltip, Runnable action, int horizontalPadding) {
        Objects.requireNonNull(action, "action");
        setToolTipText(tooltip);
        setFont(FontManager.getRunescapeSmallFont());
        setForeground(TEXT);
        setBorder(BorderFactory.createEmptyBorder(4, horizontalPadding, 4, horizontalPadding));
        setFocusPainted(false);
        setContentAreaFilled(false);
        setOpaque(false);
        setRolloverEnabled(true);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addActionListener(e -> action.run());
        getModel().addChangeListener(e -> refreshForeground());
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        refreshForeground();
    }

    private void refreshForeground() {
        if (!isEnabled()) {
            setForeground(TEXT_DISABLED);
        } else {
            setForeground(getModel().isRollover() ? TEXT_HOVER : TEXT);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (!isEnabled() && getIcon() == null) {
            paintDisabledText(g);
            return;
        }
        boolean active = isEnabled() && (getModel().isRollover() || getModel().isPressed());
        if (active || outlined) {
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(active ? Ui.SURFACE_HOVER : Ui.SURFACE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), Ui.RADIUS, Ui.RADIUS);
                if (outlined) {
                    g2.setColor(active ? ColorScheme.BRAND_ORANGE : Ui.OUTLINE);
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, Ui.RADIUS, Ui.RADIUS);
                }
            } finally {
                g2.dispose();
            }
        }
        super.paintComponent(g);
    }

    /** The look and feel embosses disabled text; a flat gray reads better on a dark panel. */
    private void paintDisabledText(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setFont(getFont());
            g2.setColor(TEXT_DISABLED);
            FontMetrics metrics = g2.getFontMetrics();
            int x = (getWidth() - metrics.stringWidth(getText())) / 2;
            int y = (getHeight() - metrics.getHeight()) / 2 + metrics.getAscent();
            g2.drawString(getText(), x, y);
        } finally {
            g2.dispose();
        }
    }
}
