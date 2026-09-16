package dev.dutchy.runelite.gear.ui;

import dev.dutchy.runelite.gear.Owner;
import dev.dutchy.runelite.gear.content.CellKind;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;

import javax.swing.*;
import java.awt.*;
import java.util.Locale;
import java.util.Objects;

/** A coloured circle with the account's initial; the colour comes from the key, so an account always looks the same. */
final class AccountBadge extends JComponent {

    static final int SIZE = 28;

    private final Owner owner;
    private final String initial;

    AccountBadge(Owner owner, String name) {
        this.owner = Objects.requireNonNull(owner, "owner");
        this.initial = name.isBlank() ? "?" : name.substring(0, 1).toUpperCase(Locale.ROOT);
        setPreferredSize(new Dimension(SIZE, SIZE));
        setMinimumSize(new Dimension(SIZE, SIZE));
        setMaximumSize(new Dimension(SIZE, SIZE));
        setOpaque(false);
    }

    static Color colourFor(Owner owner) {
        if (owner.isShared()) {
            return ColorScheme.BRAND_ORANGE;
        }
        int hue = Math.floorMod(owner.account().orElse("").hashCode(), 360);
        return Color.getHSBColor(hue / 360f, 0.45f, 0.75f);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(colourFor(owner));
            g2.fillOval(0, 0, SIZE - 1, SIZE - 1);
            if (owner.isShared()) {
                g2.setColor(new Color(30, 30, 30));
                CellGlyphs.paint(g2, CellKind.INVENTORY, 8, 7, 12, 14);
                return;
            }
            g2.setFont(FontManager.getRunescapeBoldFont());
            g2.setColor(new Color(30, 30, 30));
            FontMetrics metrics = g2.getFontMetrics();
            g2.drawString(initial, (SIZE - metrics.stringWidth(initial)) / 2, (SIZE + metrics.getAscent() - metrics.getDescent()) / 2);
        } finally {
            g2.dispose();
        }
    }
}
