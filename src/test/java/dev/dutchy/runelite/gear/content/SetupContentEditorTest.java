package dev.dutchy.runelite.gear.content;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SetupContentEditorTest {

    private static final SetupItem WHIP = SetupItem.of(4151);
    private static final SetupItem COINS = SetupItem.of(995, 1000);

    @Test
    void setsAndReadsAnEquipmentSlot() {
        SetupContent content = SetupContentEditor.withItem(GearContent.empty(), SlotRef.of(EquipmentSlot.WEAPON), WHIP);
        assertEquals(Optional.of(WHIP), SetupContentEditor.itemAt(content, SlotRef.of(EquipmentSlot.WEAPON)));
        assertEquals(Optional.empty(), SetupContentEditor.itemAt(content, SlotRef.of(EquipmentSlot.HEAD)));
    }

    @Test
    void setsAndReadsAnInventorySlot() {
        SlotRef ref = SlotRef.of(GridKind.INVENTORY, 5);
        SetupContent content = SetupContentEditor.withItem(GearContent.empty(), ref, COINS);
        assertEquals(Optional.of(COINS), SetupContentEditor.itemAt(content, ref));
    }

    @Test
    void setsBothSidesOfAGeneralSetup() {
        SetupContent content = BankContent.empty();
        content = SetupContentEditor.withItem(content, SlotRef.of(GridKind.LEFT, 0), WHIP);
        content = SetupContentEditor.withItem(content, SlotRef.of(GridKind.RIGHT, 27), COINS);

        assertEquals(Optional.of(WHIP), SetupContentEditor.itemAt(content, SlotRef.of(GridKind.LEFT, 0)));
        assertEquals(Optional.of(COINS), SetupContentEditor.itemAt(content, SlotRef.of(GridKind.RIGHT, 27)));
        assertEquals(Optional.empty(), SetupContentEditor.itemAt(content, SlotRef.of(GridKind.LEFT, 27)));
    }

    @Test
    void clearingEmptiesTheSlot() {
        SlotRef ref = SlotRef.of(EquipmentSlot.HEAD);
        SetupContent content = SetupContentEditor.withItem(GearContent.empty(), ref, WHIP);
        assertTrue(SetupContentEditor.itemAt(SetupContentEditor.cleared(content, ref), ref).isEmpty());
    }

    @Test
    void referencesThatDoNotFitTheContentAreIgnored() {
        SetupContent general = BankContent.empty();
        SlotRef equipment = SlotRef.of(EquipmentSlot.HEAD);
        assertFalse(SetupContentEditor.supports(general, equipment));
        assertSame(general, SetupContentEditor.withItem(general, equipment, WHIP));
        assertTrue(SetupContentEditor.itemAt(general, equipment).isEmpty());

        SetupContent gear = GearContent.empty();
        assertFalse(SetupContentEditor.supports(gear, SlotRef.of(GridKind.LEFT, 0)));
        assertSame(gear, SetupContentEditor.withItem(gear, SlotRef.of(GridKind.LEFT, 0), WHIP));
    }

    @Test
    void gearSupportsEquipmentAndInventoryOnly() {
        SetupContent gear = GearContent.empty();
        assertTrue(SetupContentEditor.supports(gear, SlotRef.of(EquipmentSlot.RING)));
        assertTrue(SetupContentEditor.supports(gear, SlotRef.of(GridKind.INVENTORY, 0)));
        assertFalse(SetupContentEditor.supports(gear, SlotRef.of(GridKind.RIGHT, 0)));
    }

    @Test
    void slotReferencesDescribeThemselves() {
        assertEquals("Weapon", SlotRef.of(EquipmentSlot.WEAPON).describe());
        assertEquals("Inventory slot 1", SlotRef.of(GridKind.INVENTORY, 0).describe());
    }

    @Test
    void movingSwapsWhatIsInTheWayAndCopyingLeavesTheSourceAlone() {
        SetupContent content = GearContent.empty()
                .withInventory(ItemGrid.EMPTY.withSlot(0, WHIP).withSlot(1, COINS));
        SlotRef from = SlotRef.of(GridKind.INVENTORY, 0);
        SlotRef to = SlotRef.of(GridKind.INVENTORY, 1);

        SetupContent moved = SetupContentEditor.moved(content, from, to, false);
        assertEquals(Optional.of(COINS), SetupContentEditor.itemAt(moved, from));
        assertEquals(Optional.of(WHIP), SetupContentEditor.itemAt(moved, to));

        SetupContent copied = SetupContentEditor.moved(content, from, to, true);
        assertEquals(Optional.of(WHIP), SetupContentEditor.itemAt(copied, from));
        assertEquals(Optional.of(WHIP), SetupContentEditor.itemAt(copied, to));

        SetupContent toEmpty = SetupContentEditor.moved(content, from, SlotRef.of(GridKind.INVENTORY, 5), false);
        assertTrue(SetupContentEditor.itemAt(toEmpty, from).isEmpty());
        assertSame(content, SetupContentEditor.moved(content, from, from, false));
        assertSame(content, SetupContentEditor.moved(content, SlotRef.of(GridKind.INVENTORY, 9), to, false), "nothing to move");
        assertSame(content, SetupContentEditor.moved(content, from, SlotRef.of(GridKind.LEFT, 0), false), "no such grid here");
    }

    @Test
    void quickFillOnlyTouchesEmptySlotsAfterTheSource() {
        SetupContent content = GearContent.empty()
                .withInventory(ItemGrid.EMPTY.withSlot(2, WHIP).withSlot(5, COINS));
        GearContent filled = (GearContent) SetupContentEditor.filledFrom(content, new SlotRef.Grid(GridKind.INVENTORY, 2));
        assertTrue(filled.inventory().slot(0).isEmpty(), "slots before the source stay empty");
        assertEquals(Optional.of(COINS), filled.inventory().slot(5), "filled slots are not overwritten");
        assertEquals(ItemGrid.SIZE - 2, filled.inventory().filledSlots());

        GearContent row = (GearContent) SetupContentEditor.filledRow(content, new SlotRef.Grid(GridKind.INVENTORY, 5));
        assertEquals(Optional.of(COINS), row.inventory().slot(4));
        assertEquals(Optional.of(COINS), row.inventory().slot(7));
        assertTrue(row.inventory().slot(8).isEmpty());
        assertEquals(Optional.of(WHIP), row.inventory().slot(2));
    }

    @Test
    void copyAndPasteKeepEquipmentInPlaceAndRunGridItemsFromTheAnchor() {
        SetupContent source = GearContent.empty()
                .withEquipped(EquipmentSlot.WEAPON, WHIP)
                .withInventory(ItemGrid.EMPTY.withSlot(0, COINS).withSlot(3, WHIP));
        SlotClipboard clipboard = SetupContentEditor.copied(source, List.of(
                SlotRef.of(EquipmentSlot.WEAPON), SlotRef.of(GridKind.INVENTORY, 0),
                SlotRef.of(GridKind.INVENTORY, 1), SlotRef.of(GridKind.INVENTORY, 3)));
        assertEquals(3, clipboard.size(), "empty slots are not copied");

        GearContent pasted = (GearContent) SetupContentEditor.pasted(GearContent.empty(), clipboard, SlotRef.of(GridKind.INVENTORY, 10));
        assertEquals(Optional.of(WHIP), pasted.equipped(EquipmentSlot.WEAPON));
        assertEquals(Optional.of(COINS), pasted.inventory().slot(10));
        assertEquals(Optional.of(WHIP), pasted.inventory().slot(11));

        BankContent intoBank = (BankContent) SetupContentEditor.pasted(BankContent.empty(), clipboard, SlotRef.of(GridKind.RIGHT, 27));
        assertEquals(Optional.of(COINS), intoBank.right().slot(27));
        assertTrue(intoBank.left().isEmpty(), "what does not fit past the end is dropped, equipment has no home");
    }
}
