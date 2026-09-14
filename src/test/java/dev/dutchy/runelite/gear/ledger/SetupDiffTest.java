package dev.dutchy.runelite.gear.ledger;

import dev.dutchy.runelite.gear.content.*;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SetupDiffTest {

    @Test
    void listsOnlyTheSlotsThatDiffer() {
        GearContent trident = GearContent.empty()
                .withEquipped(EquipmentSlot.WEAPON, SetupItem.of(11907))
                .withEquipped(EquipmentSlot.SHIELD, SetupItem.of(12612))
                .withInventory(ItemGrid.EMPTY.withSlot(12, SetupItem.of(6685, 4)));
        GearContent sang = trident
                .withEquipped(EquipmentSlot.WEAPON, SetupItem.of(22323))
                .withInventory(ItemGrid.EMPTY.withSlot(12, SetupItem.of(6685, 6)).withSlot(13, SetupItem.of(3024)));

        SetupDiff diff = SetupDiff.between(trident, sang);

        assertEquals(3, diff.changes().size());
        SetupDiff.SlotChange weapon = diff.changes().get(0);
        assertEquals(SlotRef.of(EquipmentSlot.WEAPON), weapon.ref());
        assertEquals(Optional.of(SetupItem.of(11907)), weapon.before());
        assertEquals(Optional.of(SetupItem.of(22323)), weapon.after());
        assertFalse(weapon.isSameItem());
        assertEquals(OptionalInt.empty(), weapon.quantityDelta());

        SetupDiff.SlotChange brews = diff.changes().get(1);
        assertTrue(brews.isSameItem());
        assertEquals(OptionalInt.of(2), brews.quantityDelta());

        SetupDiff.SlotChange added = diff.changes().get(2);
        assertTrue(added.before().isEmpty());
        assertEquals(Optional.of(SetupItem.of(3024)), added.after());
    }

    @Test
    void identicalContentsHaveNoChangesAndTypesMustMatch() {
        GearContent gear = GearContent.empty().withEquipped(EquipmentSlot.WEAPON, SetupItem.of(4151));
        assertTrue(SetupDiff.between(gear, gear).isEmpty());
        assertFalse(SetupDiff.comparable(gear, BankContent.empty()));
        assertThrows(IllegalArgumentException.class, () -> SetupDiff.between(gear, BankContent.empty()));
    }
    @Test
    void customLayoutsCompareCellByCellWhenTheyShareARowCount() {
        CustomContent before = CustomContent.empty(1)
                .withCell(CellRef.of(0, 0), LayoutCell.equipment(Map.of(EquipmentSlot.WEAPON, SetupItem.of(4151))))
                .withCell(CellRef.of(0, 1), LayoutCell.inventory(ItemGrid.EMPTY.withSlot(0, SetupItem.of(385, 4))));
        CustomContent after = before
                .withCell(CellRef.of(0, 0), LayoutCell.equipment(Map.of(EquipmentSlot.WEAPON, SetupItem.of(11791))))
                .withCell(CellRef.of(0, 1), LayoutCell.inventory(ItemGrid.EMPTY.withSlot(0, SetupItem.of(385, 6))));

        assertTrue(SetupDiff.comparable(before, after));
        assertFalse(SetupDiff.comparable(before, CustomContent.empty(2)));
        SetupDiff diff = SetupDiff.between(before, after);
        assertEquals(2, diff.changes().size());
        assertEquals("Row 1 left, Weapon", diff.changes().get(0).ref().describe());
        assertEquals(OptionalInt.of(2), diff.changes().get(1).quantityDelta());

        SetupDiff kindChange = SetupDiff.between(before, before.withCell(CellRef.of(0, 1), LayoutCell.EMPTY));
        assertEquals(1, kindChange.changes().size());
        assertTrue(kindChange.changes().get(0).after().isEmpty(), "an emptied cell reads as removed items");
    }

}
