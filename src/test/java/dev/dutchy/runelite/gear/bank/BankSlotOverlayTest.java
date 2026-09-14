package dev.dutchy.runelite.gear.bank;

import dev.dutchy.runelite.gear.content.ItemQuantityFormat;
import dev.dutchy.runelite.gear.content.SetupItem;
import dev.dutchy.runelite.gear.items.ItemCharges;
import dev.dutchy.runelite.gear.items.StaticItemCharges;
import dev.dutchy.runelite.libs.ui.item.ItemId;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BankSlotOverlayTest {

    private static final int POTION_4 = 2434;
    private static final int POTION_3 = 139;
    private static final int POTION_1 = 143;

    private static DrawnSlot drawn(int required, int held) {
        SlotSupply supply = held > 0 ? SlotSupply.of(new SlotSupply.Stack(ItemId.of(995), held)) : SlotSupply.NONE;
        return new DrawnSlot(new BankSlotPlan(0, SetupItem.of(995, required), supply), held > 0 ? 3 : -1);
    }

    /** Two four-dose, six three-dose and one one-dose potion held for a slot that asks for {@code required}. */
    private static DrawnSlot potions(int required) {
        SlotSupply supply = SlotSupply.of(new SlotSupply.Stack(ItemId.of(POTION_4), 2), new SlotSupply.Stack(ItemId.of(POTION_3), 6),
                new SlotSupply.Stack(ItemId.of(POTION_1), 1));
        return new DrawnSlot(new BankSlotPlan(0, SetupItem.of(POTION_4, required), supply), 3);
    }

    @Test
    void everySlotWithAnAmountReadsHeldOverRequiredTheWayTheGameWritesStacks() {
        assertEquals("1000K/20M", BankSlotOverlay.amountText(drawn(20_000_000, 1_000_000)));
        assertEquals("500/500", BankSlotOverlay.amountText(drawn(500, 500)));
        assertEquals("0/12", BankSlotOverlay.amountText(drawn(12, 0)));
        assertEquals("100K/50000", BankSlotOverlay.amountText(drawn(50_000, 100_000)));
    }

    @Test
    void theColourIsRedWhileShortAndTheGamesStackColourOtherwise() {
        assertEquals(BankSlotOverlay.SHORT, BankSlotOverlay.amountColor(drawn(500, 499)));
        assertEquals(ItemQuantityFormat.SMALL, BankSlotOverlay.amountColor(drawn(500, 500)));
        assertEquals(ItemQuantityFormat.THOUSANDS, BankSlotOverlay.amountColor(drawn(50_000, 100_000)));
        assertEquals(ItemQuantityFormat.MILLIONS, BankSlotOverlay.amountColor(drawn(1, 10_000_000)));
    }

    @Test
    void aFamilyCountsAsOneAndGoesAmberWhenOnlyTheOtherDosesMakeUpTheNeed() {
        DrawnSlot slot = potions(4);
        assertEquals("9/4", BankSlotOverlay.amountText(slot), "every dose counts");
        assertEquals(BankSlotOverlay.PARTIAL, BankSlotOverlay.amountColor(slot), "the drawn (4)s alone fall short");
        assertEquals(ItemQuantityFormat.SMALL, BankSlotOverlay.amountColor(potions(2)), "the drawn stack covers it");
        assertEquals(BankSlotOverlay.SHORT, BankSlotOverlay.amountColor(potions(10)), "not even the family covers it");
    }

    @Test
    void theTooltipListsEachStackWithTheDrawnOneFirstAndSumsTheDoses() {
        ItemCharges charges = new StaticItemCharges().charged(POTION_4, 4).charged(POTION_3, 3).charged(POTION_1, 1);
        String text = BankSlotOverlay.tooltipText(potions(4), id -> "Prayer potion(" + charges.chargesOf(id).orElseThrow() + ")", charges);
        assertEquals("<col=ff9040>Prayer potion(4) x2</col></br>Prayer potion(3) x6</br>Prayer potion(1) x1</br>Total 9 · 27 doses/charges</br>Right-click to show another here", text);

        String uncounted = BankSlotOverlay.tooltipText(potions(4), id -> "Ring", ItemCharges.none());
        assertEquals("Total 9", uncounted.split("</br>")[3], "no dose sum when a member has no count");
    }
}
