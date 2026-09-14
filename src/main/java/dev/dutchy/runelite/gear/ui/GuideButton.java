package dev.dutchy.runelite.gear.ui;

import net.runelite.client.ui.ColorScheme;

import java.awt.*;

/** The ? that starts a page's guide, with a quiet dot until that guide has been completed once. */
final class GuideButton extends FlatButton {

    static final int SIZE = 24;
    private static final int DOT = 5;

    private final boolean unseen;

    GuideButton(Runnable onGuide, boolean unseen) {
        super(ActionIcon.HELP, "Guide: a step-by-step look at this page", onGuide);
        this.unseen = unseen;
        setPreferredSize(new Dimension(SIZE, SIZE));
    }

    boolean isUnseen() {
        return unseen;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (!unseen) {
            return;
        }
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(ColorScheme.BRAND_ORANGE);
            g2.fillOval(getWidth() - DOT - 3, 3, DOT, DOT);
        } finally {
            g2.dispose();
        }
    }
}
