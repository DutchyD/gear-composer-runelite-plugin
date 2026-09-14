package dev.dutchy.runelite.gear.content;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CustomContentTest {

    private static final SetupItem WHIP = SetupItem.of(4151);
    private static final SetupItem SHARK = SetupItem.of(385, 4);
    private static final CellRef TOP_LEFT = CellRef.of(0, 0);
    private static final CellRef TOP_RIGHT = CellRef.of(0, 1);
    private static final CellRef BOTTOM_LEFT = CellRef.of(1, 0);

    @Test
    void aFreshLayoutIsAllEmptyCellsOfTheChosenRows() {
        CustomContent content = CustomContent.empty(3);
        assertEquals(SetupType.CUSTOM, content.type());
        assertEquals(6, content.cells().size());
        assertTrue(content.isEmpty());
        assertTrue(content.cellRefs().stream().allMatch(ref -> content.cell(ref).isBlank()));
        assertEquals(List.of(TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, CellRef.of(1, 1), CellRef.of(2, 0), CellRef.of(2, 1)), content.cellRefs());
        assertThrows(IllegalArgumentException.class, () -> CustomContent.empty(0));
        assertThrows(IllegalArgumentException.class, () -> CustomContent.empty(4));
        assertThrows(IllegalArgumentException.class, () -> CustomContent.empty(1).cell(BOTTOM_LEFT));
    }

    @Test
    void cellsHoldOneKindOfThingAndKeepTheirNameAcrossKinds() {
        LayoutCell equipment = LayoutCell.equipment(Map.of(EquipmentSlot.WEAPON, WHIP)).withName("Vorkath");
        assertEquals("Vorkath", equipment.label());
        assertEquals(Optional.of("Vorkath"), equipment.name());
        assertEquals("Equipment", LayoutCell.equipment(Map.of()).label());
        assertEquals(List.of(WHIP), equipment.items());
        assertThrows(IllegalArgumentException.class, () -> new LayoutCell(CellKind.INVENTORY, null, Map.of(EquipmentSlot.WEAPON, WHIP), ItemGrid.EMPTY));
        assertThrows(IllegalArgumentException.class, () -> new LayoutCell(CellKind.EMPTY, null, Map.of(), ItemGrid.EMPTY.withSlot(0, SHARK)));
        assertThrows(IllegalArgumentException.class, () -> equipment.withName(" "));

        LayoutCell inventory = equipment.withKind(CellKind.INVENTORY);
        assertEquals(CellKind.INVENTORY, inventory.kind());
        assertTrue(inventory.isEmpty());
        assertEquals("Vorkath", inventory.label(), "the name survives a change of kind");
        assertTrue(equipment.cleared().isEmpty());
        assertEquals("Vorkath", equipment.cleared().label());
    }

    @Test
    void slotsInsideCellsReadAndWriteThroughTheEditor() {
        CustomContent content = CustomContent.empty(2)
                .withCell(TOP_LEFT, LayoutCell.equipment(Map.of()))
                .withCell(TOP_RIGHT, LayoutCell.inventory(ItemGrid.EMPTY));
        SlotRef weapon = SlotRef.in(TOP_LEFT, SlotRef.of(EquipmentSlot.WEAPON));
        SlotRef pocket = SlotRef.in(TOP_RIGHT, SlotRef.of(GridKind.INVENTORY, 3));
        SlotRef wrongKind = SlotRef.in(TOP_LEFT, SlotRef.of(GridKind.INVENTORY, 0));
        SlotRef blank = SlotRef.in(BOTTOM_LEFT, SlotRef.of(EquipmentSlot.WEAPON));

        assertTrue(SetupContentEditor.supports(content, weapon));
        assertTrue(SetupContentEditor.supports(content, pocket));
        assertFalse(SetupContentEditor.supports(content, wrongKind));
        assertFalse(SetupContentEditor.supports(content, blank));
        assertEquals("Row 1 left, Weapon", weapon.describe());
        assertEquals("Row 1 right, Inventory slot 4", pocket.describe());

        SetupContent filled = SetupContentEditor.withItem(SetupContentEditor.withItem(content, weapon, WHIP), pocket, SHARK);
        assertEquals(Optional.of(WHIP), SetupContentEditor.itemAt(filled, weapon));
        assertEquals(Optional.of(SHARK), SetupContentEditor.itemAt(filled, pocket));
        assertEquals(List.of(WHIP, SHARK), SetupContentEditor.allItems(filled));
        assertEquals(content, SetupContentEditor.withItem(content, wrongKind, SHARK), "a slot the cell lacks is ignored");
        assertTrue(SetupContentEditor.itemAt(SetupContentEditor.cleared(filled, weapon), weapon).isEmpty());

        SetupContent moved = SetupContentEditor.moved(filled, pocket, SlotRef.in(TOP_RIGHT, SlotRef.of(GridKind.INVENTORY, 0)), false);
        assertEquals(Optional.of(SHARK), SetupContentEditor.itemAt(moved, SlotRef.in(TOP_RIGHT, SlotRef.of(GridKind.INVENTORY, 0))));
        assertTrue(SetupContentEditor.itemAt(moved, pocket).isEmpty());
    }

    @Test
    void fillAndPasteStayInsideTheCellsGrid() {
        CustomContent content = CustomContent.empty(1)
                .withCell(TOP_LEFT, LayoutCell.inventory(ItemGrid.EMPTY.withSlot(0, SHARK)))
                .withCell(TOP_RIGHT, LayoutCell.inventory(ItemGrid.EMPTY));
        SlotRef first = SlotRef.in(TOP_LEFT, SlotRef.of(GridKind.INVENTORY, 0));
        assertTrue(first.inGrid());
        assertEquals(SlotRef.in(TOP_LEFT, SlotRef.of(GridKind.INVENTORY, 5)), first.gridSibling(5));

        CustomContent row = (CustomContent) SetupContentEditor.filledRow(content, first);
        assertEquals(4, row.cell(TOP_LEFT).items().size());
        assertTrue(row.cell(TOP_RIGHT).isEmpty(), "the other cell is untouched");
        CustomContent rest = (CustomContent) SetupContentEditor.filledFrom(content, first);
        assertEquals(ItemGrid.SIZE, rest.cell(TOP_LEFT).items().size());

        SlotClipboard copied = SetupContentEditor.copied(row, List.of(first, first.gridSibling(1)));
        SetupContent pasted = SetupContentEditor.pasted(content, copied, SlotRef.in(TOP_RIGHT, SlotRef.of(GridKind.INVENTORY, 26)));
        assertEquals(Optional.of(SHARK), SetupContentEditor.itemAt(pasted, SlotRef.in(TOP_RIGHT, SlotRef.of(GridKind.INVENTORY, 26))));
        assertEquals(Optional.of(SHARK), SetupContentEditor.itemAt(pasted, SlotRef.in(TOP_RIGHT, SlotRef.of(GridKind.INVENTORY, 27))));
    }

    @Test
    void rowsGrowWithEmptyCellsAndShrinkWithAWarningSignal() {
        CustomContent content = CustomContent.empty(1).withCell(TOP_LEFT, LayoutCell.equipment(Map.of(EquipmentSlot.WEAPON, WHIP)));
        CustomContent grown = content.withRows(3);
        assertEquals(6, grown.cells().size());
        assertEquals(content.cell(TOP_LEFT), grown.cell(TOP_LEFT));
        assertFalse(grown.hasItemsBelow(1));

        CustomContent tall = grown.withCell(CellRef.of(2, 1), LayoutCell.inventory(ItemGrid.EMPTY.withSlot(0, SHARK)));
        assertTrue(tall.hasItemsBelow(2));
        assertEquals(List.of(WHIP), tall.withRows(2).allItems());
    }
}
