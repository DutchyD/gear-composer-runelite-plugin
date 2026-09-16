package dev.dutchy.runelite.gear.bank;

import dev.dutchy.runelite.gear.content.QuantityText;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/** Picks the bank's withdraw entry that hands over exactly the amount a setup asks for. */
public final class WithdrawOption {

    private static final String PREFIX = "Withdraw-";
    private static final String ALL = "Withdraw-All";

    private WithdrawOption() {
    }

    /** The menu option to prefer for {@code amount}, if the bank offers one that matches. */
    public static Optional<String> preferred(int amount, List<String> options) {
        Objects.requireNonNull(options, "options");
        if (amount == QuantityText.MAX && options.contains(ALL)) {
            return Optional.of(ALL);
        }
        String exact = PREFIX + amount;
        return options.stream().filter(exact::equals).findFirst();
    }
}
