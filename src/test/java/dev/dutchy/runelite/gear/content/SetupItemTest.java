package dev.dutchy.runelite.gear.content;

import dev.dutchy.runelite.libs.ui.item.ItemId;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.OptionalInt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SetupItemTest {

    @Test
    void anItemWithoutAnAmountFollowsTheBank() {
        SetupItem whip = SetupItem.of(4151);
        assertFalse(whip.hasQuantity());
        assertEquals(OptionalInt.empty(), whip.quantity());
        assertFalse(whip.showsQuantity(), "no number is drawn when the bank amount is used");
    }

    @Test
    void anItemCanCarryAnExplicitAmount() {
        SetupItem coins = SetupItem.of(995, 250_000);
        assertTrue(coins.hasQuantity());
        assertEquals(250_000, coins.quantity().orElseThrow());
        assertTrue(coins.showsQuantity());
    }

    @Test
    void anAmountOfOneDrawsNoNumber() {
        assertTrue(SetupItem.of(4151, 1).hasQuantity());
        assertFalse(SetupItem.of(4151, 1).showsQuantity());
    }

    @Test
    void rejectsAmountsBelowOne() {
        assertThrows(IllegalArgumentException.class, () -> SetupItem.of(995, 0));
        assertThrows(IllegalArgumentException.class, () -> SetupItem.of(995, -5));
    }

    @Test
    void anAmountCanBeSetAndTakenAway() {
        SetupItem coins = SetupItem.of(995).withQuantity(10);
        assertEquals(10, coins.quantity().orElseThrow());
        assertFalse(coins.withoutQuantity().hasQuantity());
    }

    @Test
    void theShownAmountNeverExceedsWhatTheBankHolds() {
        assertEquals(500, SetupItem.of(995, 500).shownQuantity(10_000));
        assertEquals(120, SetupItem.of(995, 500).shownQuantity(120));
        assertEquals(10_000, SetupItem.of(995).shownQuantity(10_000));
    }

    @Test
    void alternativesAreOrderedDistinctAndNeverTheItemItself() {
        SetupItem item = SetupItem.of(4151)
                .withAlternative(ItemId.of(4153))
                .withAlternative(ItemId.of(4151))
                .withAlternative(ItemId.of(4153))
                .withAlternative(ItemId.of(1333));
        assertEquals(List.of(ItemId.of(4153), ItemId.of(1333)), item.alternatives());
        assertEquals(List.of(ItemId.of(4151), ItemId.of(4153), ItemId.of(1333)), item.candidates());
        assertEquals(List.of(ItemId.of(1333)), item.withoutAlternative(ItemId.of(4153)).alternatives());
        assertTrue(item.hasAlternatives());
    }

    @Test
    void itemsMatchAnyVariantUnlessToldOtherwise() {
        assertEquals(ItemMatch.ANY_VARIANT, SetupItem.of(4151).match());
        assertEquals(ItemMatch.EXACT, SetupItem.of(4151).withMatch(ItemMatch.EXACT).match());
        assertEquals(ItemMatch.EXACT, SetupItem.of(4151, 5).withMatch(ItemMatch.EXACT).withQuantity(6).match(),
                "changing the amount keeps the match rule");
    }

    @Test
    void tooManyAlternativesAreRefused() {
        SetupItem item = SetupItem.of(1);
        for (int id = 2; id <= SetupItem.MAX_ALTERNATIVES + 1; id++) {
            item = item.withAlternative(ItemId.of(id));
        }
        SetupItem full = item;
        assertThrows(IllegalArgumentException.class, () -> full.withAlternative(ItemId.of(99)));
    }

    @Test
    void aSlotCanAskForTheNotedForm() {
        SetupItem bones = SetupItem.of(526, 28);
        assertFalse(bones.noted());
        SetupItem noted = bones.withNoted(true);
        assertTrue(noted.noted());
        assertEquals(28, noted.quantity().orElseThrow(), "the rest is untouched");
        assertTrue(noted.withQuantity(5).noted(), "the flag survives other changes");
    }
}
