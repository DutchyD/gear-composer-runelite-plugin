package dev.dutchy.runelite.gear.ui;

import javax.swing.*;
import java.awt.*;

/** A card stack sized to the card on show, not to the tallest card it has ever held. */
final class CardHost extends JPanel {

    CardHost(CardLayout cards) {
        super(cards);
    }

    @Override
    public Dimension getPreferredSize() {
        for (Component card : getComponents()) {
            if (card.isVisible()) {
                Dimension size = card.getPreferredSize();
                Insets insets = getInsets();
                return new Dimension(size.width + insets.left + insets.right, size.height + insets.top + insets.bottom);
            }
        }
        return super.getPreferredSize();
    }

    @Override
    public Dimension getMinimumSize() {
        return getPreferredSize();
    }
}
