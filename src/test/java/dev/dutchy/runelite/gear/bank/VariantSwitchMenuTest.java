package dev.dutchy.runelite.gear.bank;

import dev.dutchy.runelite.gear.content.SetupItem;
import dev.dutchy.runelite.libs.ui.item.ItemId;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VariantSwitchMenuTest {

    @Test
    void everyStackButTheDrawnOneGetsAnEntryNamingItAndItsCount() {
        SlotSupply supply = SlotSupply.of(new SlotSupply.Stack(ItemId.of(2434), 2), new SlotSupply.Stack(ItemId.of(139), 6000),
                new SlotSupply.Stack(ItemId.of(143), 1));
        DrawnSlot slot = new DrawnSlot(new BankSlotPlan(5, SetupItem.of(2434, 4), supply), 3);

        List<VariantSwitchMenu.Entry> entries = VariantSwitchMenu.entriesFor(slot, id -> "Potion " + id.value());

        assertEquals(List.of("<col=ff9040>Potion 139</col> x6000", "<col=ff9040>Potion 143</col> x1"),
                entries.stream().map(VariantSwitchMenu.Entry::target).collect(Collectors.toList()));
        assertEquals(ItemId.of(139), entries.get(0).id());
        assertTrue(VariantSwitchMenu.entriesFor(new DrawnSlot(new BankSlotPlan(5, SetupItem.of(2434), SlotSupply.NONE), -1), id -> "").isEmpty());
    }
}
