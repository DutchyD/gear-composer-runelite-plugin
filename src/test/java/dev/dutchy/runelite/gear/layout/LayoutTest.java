package dev.dutchy.runelite.gear.layout;

import dev.dutchy.runelite.gear.content.*;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LayoutTest {

    private static final SetupItem WHIP = SetupItem.of(4151);
    private static final SetupItem SHARK = SetupItem.of(385, 4);

    @Test
    void aGearLayoutIsACrossBesideAnInventory() {
        GearContent gear = GearContent.empty()
                .withEquipped(EquipmentSlot.WEAPON, WHIP)
                .withInventory(ItemGrid.EMPTY.withSlot(27, SHARK).withDivider(0, "Food"));
        Layout layout = Layout.of(gear);

        List<LayoutBlock> blocks = layout.blocks();
        assertEquals(List.of("Equipment", "Inventory"), blocks.stream().map(LayoutBlock::title).collect(Collectors.toList()));
        assertEquals(BankSide.LEFT, blocks.get(0).side());
        assertEquals(BankSide.RIGHT, blocks.get(1).side());
        assertEquals(0, blocks.get(0).band());
        assertEquals(0, blocks.get(1).band());
        assertEquals(ItemGrid.ROWS, blocks.get(1).rowsTall(), "a divider takes no row of its own");
        assertEquals(ItemGrid.ROWS, layout.rowsTall());
        assertEquals(List.of(0), List.copyOf(layout.headerRows()), "the divider's row grows a header");
        assertTrue(layout.labels().get(0).header());
        assertEquals(List.of(WHIP, SHARK), layout.items());
        assertEquals(EquipmentSlot.values().length + ItemGrid.SIZE, layout.slots().size());
        assertFalse(layout.hasLabelledBands(), "a divider is not a band label");

        List<LayoutLabel> labels = layout.labels();
        assertEquals(List.of("Food"), labels.stream().map(LayoutLabel::text).collect(Collectors.toList()));
        assertEquals(0, labels.get(0).row());
        assertEquals(BankSide.RIGHT, labels.get(0).side());
    }

    @Test
    void aBankLayoutIsTwoGridsAndACustomLayoutIsBandsWithTitles() {
        Layout bank = Layout.of(BankContent.empty().withLeft(ItemGrid.EMPTY.withSlot(0, SHARK)));
        assertEquals(List.of(GridKind.LEFT, GridKind.RIGHT), bank.blocks().stream().map(block -> ((GridBlock) block).kind()).collect(Collectors.toList()));
        assertEquals(1, bank.bands().size());
        assertEquals(SlotRef.of(GridKind.RIGHT, 3), ((GridBlock) bank.blocks().get(1)).ref(3));

        CustomContent custom = CustomContent.empty(3)
                .withCell(CellRef.of(0, 0), LayoutCell.equipment(Map.of(EquipmentSlot.WEAPON, WHIP)).withName("Melee"))
                .withCell(CellRef.of(0, 1), LayoutCell.inventory(ItemGrid.EMPTY.withSlot(0, SHARK)))
                .withCell(CellRef.of(2, 1), LayoutCell.equipment(Map.of()).withName("Spare"));
        Layout layout = Layout.of(custom);
        List<Band> bands = layout.bands();
        assertEquals(2, bands.size(), "an empty grid row takes no band");
        assertEquals(0, bands.get(0).labelRow().orElseThrow());
        assertEquals(1, bands.get(0).firstRow());
        assertEquals(ItemGrid.ROWS, bands.get(0).rowsTall(), "a band is as tall as its tallest block");
        assertEquals(1 + ItemGrid.ROWS, bands.get(1).labelRow().orElseThrow());
        assertEquals("Melee", bands.get(0).on(BankSide.LEFT).orElseThrow().title());
        assertEquals("Inventory", bands.get(0).on(BankSide.RIGHT).orElseThrow().title());
        assertEquals(SlotRef.in(CellRef.of(0, 0), SlotRef.of(EquipmentSlot.WEAPON)), ((EquipmentBlock) bands.get(0).blocks().get(0)).ref(EquipmentSlot.WEAPON));
        assertFalse(((EquipmentBlock) bands.get(0).blocks().get(0)).countsWornBonuses());
        assertTrue(layout.hasLabelledBands());
        assertEquals(List.of("Melee", "Inventory", "Spare"), layout.labels().stream().map(LayoutLabel::text).collect(Collectors.toList()));
        assertEquals(1, layout.placements().stream().filter(p -> p.side() == BankSide.LEFT).count());
        assertEquals(1 + EquipmentSlot.WEAPON.row(), layout.placements().get(0).row());
    }
}
