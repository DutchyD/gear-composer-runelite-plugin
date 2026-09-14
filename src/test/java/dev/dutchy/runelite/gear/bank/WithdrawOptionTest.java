package dev.dutchy.runelite.gear.bank;

import dev.dutchy.runelite.gear.content.QuantityText;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WithdrawOptionTest {

    private static final List<String> OPTIONS = List.of("Withdraw-1", "Withdraw-5", "Withdraw-10", "Withdraw-X", "Withdraw-25", "Withdraw-All", "Examine");

    @Test
    void matchesTheStandardAmountsAndTheCurrentCustomAmount() {
        assertEquals(Optional.of("Withdraw-1"), WithdrawOption.preferred(1, OPTIONS));
        assertEquals(Optional.of("Withdraw-10"), WithdrawOption.preferred(10, OPTIONS));
        assertEquals(Optional.of("Withdraw-25"), WithdrawOption.preferred(25, OPTIONS), "the game lists the last custom amount");
        assertEquals(Optional.of("Withdraw-All"), WithdrawOption.preferred(QuantityText.MAX, OPTIONS));
    }

    @Test
    void anAmountTheBankDoesNotOfferHasNoPreference() {
        assertEquals(Optional.empty(), WithdrawOption.preferred(6, OPTIONS));
    }
}
