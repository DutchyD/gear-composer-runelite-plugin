package dev.dutchy.runelite.gear.ledger;

import java.util.Locale;

/** Short, game-style readings: 41.2M coins, 18.4 kg, +112. */
public final class LedgerFormat {

    private LedgerFormat() {
    }

    public static String coins(long value) {
        long magnitude = Math.abs(value);
        String sign = value < 0 ? "-" : "";
        if (magnitude >= 1_000_000_000L) {
            return sign + trim(magnitude / 1_000_000_000d) + "B";
        }
        if (magnitude >= 1_000_000L) {
            return sign + trim(magnitude / 1_000_000d) + "M";
        }
        if (magnitude >= 10_000L) {
            return sign + trim(magnitude / 1_000d) + "K";
        }
        return sign + magnitude;
    }

    public static String coinsDelta(long delta) {
        return delta > 0 ? "+" + coins(delta) : coins(delta);
    }

    public static String weight(double kilograms) {
        return String.format(Locale.ROOT, "%.1f kg", kilograms);
    }

    public static String weightDelta(double delta) {
        return (delta > 0 ? "+" : "") + String.format(Locale.ROOT, "%.1f", delta);
    }

    public static String signed(int value) {
        return value > 0 ? "+" + value : String.valueOf(value);
    }

    public static String signed(double value) {
        String text = String.format(Locale.ROOT, "%.1f", value);
        return value > 0 ? "+" + text : text;
    }

    private static String trim(double value) {
        String text = String.format(Locale.ROOT, "%.1f", value);
        return text.endsWith(".0") ? text.substring(0, text.length() - 2) : text;
    }
}
