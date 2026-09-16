package dev.dutchy.runelite.gear.content;

import java.awt.*;

/** Stack sizes as the game draws them. */
public final class ItemQuantityFormat {

    private static final int THOUSAND = 1_000;
    private static final int MILLION = 1_000_000;
    private static final int K_THRESHOLD = 100_000;
    private static final int M_THRESHOLD = 10_000_000;

    public static final Color SMALL = new Color(255, 255, 0);
    public static final Color THOUSANDS = Color.WHITE;
    public static final Color MILLIONS = new Color(0, 255, 128);

    private ItemQuantityFormat() {
    }

    public static String text(int quantity) {
        if (quantity < K_THRESHOLD) {
            return String.valueOf(quantity);
        }
        if (quantity < M_THRESHOLD) {
            return quantity / THOUSAND + "K";
        }
        return quantity / MILLION + "M";
    }

    public static Color color(int quantity) {
        if (quantity < K_THRESHOLD) {
            return SMALL;
        }
        return quantity < M_THRESHOLD ? THOUSANDS : MILLIONS;
    }
}
