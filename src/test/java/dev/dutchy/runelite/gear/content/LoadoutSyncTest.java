package dev.dutchy.runelite.gear.content;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LoadoutSyncTest {

    private static final SetupItem WHIP = SetupItem.of(4151, 1);
    private static final SetupItem SHARK = SetupItem.of(385, 1);
    private static final Loadout LOADOUT = new Loadout(Map.of(EquipmentSlot.WEAPON, WHIP),
            ItemGrid.EMPTY.withSlot(3, SHARK));

    @Test
    void aGearSetupTakesBothTheWornAndCarriedItems() {
        GearContent before = GearContent.empty()
                .withEquipped(EquipmentSlot.HEAD, SetupItem.of(1163))
                .withInventory(ItemGrid.EMPTY.withSlot(0, SetupItem.of(995, 100)));

        GearContent after = (GearContent) LoadoutSync.apply(before, SyncScope.GEAR, LOADOUT);

        assertEquals(Optional.of(WHIP), after.equipped(EquipmentSlot.WEAPON));
        assertTrue(after.equipped(EquipmentSlot.HEAD).isEmpty(), "slots the player has empty are emptied too");
        assertEquals(LOADOUT.inventory(), after.inventory());
    }

    @Test
    void aBankSideTakesOnlyTheCarriedItems() {
        BankContent before = BankContent.empty()
                .withLeft(ItemGrid.EMPTY.withSlot(0, SetupItem.of(995, 100)))
                .withRight(ItemGrid.EMPTY.withSlot(1, SetupItem.of(995, 100)));

        BankContent left = (BankContent) LoadoutSync.apply(before, SyncScope.LEFT_SIDE, LOADOUT);
        assertEquals(LOADOUT.inventory(), left.left());
        assertEquals(before.right(), left.right(), "the other side is untouched");

        BankContent right = (BankContent) LoadoutSync.apply(before, SyncScope.RIGHT_SIDE, LOADOUT);
        assertEquals(before.left(), right.left());
        assertEquals(LOADOUT.inventory(), right.right());
    }

    @Test
    void aScopeThatDoesNotFitTheSetupIsRefused() {
        assertThrows(IllegalArgumentException.class,
                () -> LoadoutSync.apply(GearContent.empty(), SyncScope.LEFT_SIDE, LOADOUT));
        assertThrows(IllegalArgumentException.class,
                () -> LoadoutSync.apply(BankContent.empty(), SyncScope.GEAR, LOADOUT));
    }
}
