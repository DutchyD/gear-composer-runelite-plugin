package dev.dutchy.runelite.gear.bank;

import dev.dutchy.runelite.gear.content.SetupItem;
import dev.dutchy.runelite.gear.layout.BankSide;
import dev.dutchy.runelite.gear.layout.LayoutLabel;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DrawnBankTest {

    @Test
    void answersEveryReaderFromOneFrozenSnapshot() {
        BankSlotPlan shark = new BankSlotPlan(3, SetupItem.of(385, 4), SlotSupply.of(new SlotSupply.Stack(SetupItem.of(385).id(), 2)));
        BankSlotPlan notedBar = new BankSlotPlan(4, SetupItem.of(2358, 10).withNoted(true), SlotSupply.NONE);
        DrawnBank bank = new DrawnBank(true, Map.of(7, new DrawnSlot(shark, 40), 8, new DrawnSlot(notedBar, -1)),
                List.of(new LayoutLabel(BankSide.LEFT, 2, "Food")), BankRows.plain(), "shar");

        assertTrue(bank.active());
        assertTrue(bank.at(7).orElseThrow().isShort());
        assertTrue(bank.at(9).isEmpty());
        assertTrue(bank.wantsNotes());
        assertEquals("Food", bank.labels().get(0).text());
        assertEquals("shar", bank.searchQuery());

        assertFalse(DrawnBank.NONE.active());
        assertFalse(DrawnBank.NONE.wantsNotes());
        assertTrue(DrawnBank.NONE.at(7).isEmpty());
    }
}
