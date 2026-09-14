package dev.dutchy.runelite.gear.content;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.Map;
import java.util.OptionalInt;

/** Amounts as players type them: plain numbers, K/M/B suffixes, or MAX for the largest stack the game allows. */
public final class QuantityText {

    public static final int MAX = Integer.MAX_VALUE;
    public static final String MAX_TEXT = "MAX";

    private static final Map<Character, BigDecimal> SUFFIXES = Map.of(
            'K', BigDecimal.valueOf(1_000),
            'M', BigDecimal.valueOf(1_000_000),
            'B', BigDecimal.valueOf(1_000_000_000));

    private QuantityText() {
    }

    public static OptionalInt parse(String text) {
        String trimmed = text == null ? "" : text.strip().toUpperCase(Locale.ROOT);
        if (trimmed.isEmpty()) {
            return OptionalInt.empty();
        }
        if (MAX_TEXT.equals(trimmed)) {
            return OptionalInt.of(MAX);
        }
        BigDecimal multiplier = SUFFIXES.get(trimmed.charAt(trimmed.length() - 1));
        String digits = multiplier == null ? trimmed : trimmed.substring(0, trimmed.length() - 1);
        try {
            BigDecimal value = new BigDecimal(digits).multiply(multiplier == null ? BigDecimal.ONE : multiplier);
            if (value.stripTrailingZeros().scale() > 0
                    || value.compareTo(BigDecimal.valueOf(SetupItem.MIN_QUANTITY)) < 0
                    || value.compareTo(BigDecimal.valueOf(MAX)) > 0) {
                return OptionalInt.empty();
            }
            return OptionalInt.of(value.intValueExact());
        } catch (NumberFormatException | ArithmeticException e) {
            return OptionalInt.empty();
        }
    }
}
