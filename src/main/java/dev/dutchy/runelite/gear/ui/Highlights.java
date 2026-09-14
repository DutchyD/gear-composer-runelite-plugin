package dev.dutchy.runelite.gear.ui;

import net.runelite.client.ui.ColorScheme;

import java.awt.*;

final class Highlights {

    private static final int TINT_ALPHA = ColorScheme.BRAND_ORANGE_TRANSPARENT.getAlpha();

    private Highlights() {
    }

    /** Opaque blend of the brand tint over the given surface; an opaque component must not paint translucent pixels. */
    static Color selection(Color surface) {
        return blend(ColorScheme.BRAND_ORANGE_TRANSPARENT, surface);
    }

    /** Opaque blend of the danger tint, marking what a bulk delete will remove. */
    static Color danger(Color surface) {
        return blend(new Color(Ui.DANGER.getRed(), Ui.DANGER.getGreen(), Ui.DANGER.getBlue(), TINT_ALPHA), surface);
    }

    private static Color blend(Color over, Color under) {
        double alpha = over.getAlpha() / 255.0;
        return new Color(
                channel(over.getRed(), under.getRed(), alpha),
                channel(over.getGreen(), under.getGreen(), alpha),
                channel(over.getBlue(), under.getBlue(), alpha));
    }

    private static int channel(int over, int under, double alpha) {
        return (int) Math.round(over * alpha + under * (1.0 - alpha));
    }
}
