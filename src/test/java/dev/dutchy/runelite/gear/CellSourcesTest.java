package dev.dutchy.runelite.gear;

import dev.dutchy.runelite.gear.content.*;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CellSourcesTest {

    @Test
    void offersEveryMatchingNonEmptyPartExceptTheSetupItself() {
        GearSetup gear = GearSetup.named("Vorkath").withContent(GearContent.empty()
                .withEquipped(EquipmentSlot.WEAPON, SetupItem.of(4151))
                .withInventory(ItemGrid.EMPTY.withSlot(0, SetupItem.of(385))));
        GearSetup bank = GearSetup.named("Herbs").withContent(BankContent.empty().withRight(ItemGrid.EMPTY.withSlot(0, SetupItem.of(207))));
        GearSetup custom = GearSetup.named("Trip").withContent(CustomContent.empty(1)
                .withCell(CellRef.of(0, 0), LayoutCell.equipment(Map.of(EquipmentSlot.HEAD, SetupItem.of(1163))).withName("Melee"))
                .withCell(CellRef.of(0, 1), LayoutCell.inventory(ItemGrid.EMPTY)));
        List<GearSection> sections = List.of(new GearSection(SectionId.random(), "S", List.of(gear, bank, custom)));

        List<String> equipment = CellSources.of(sections, CellKind.EQUIPMENT, bank.id()).stream().map(CellSources.Source::label).collect(Collectors.toList());
        assertEquals(List.of("Vorkath · Equipment", "Trip · Melee"), equipment);

        List<String> inventories = CellSources.of(sections, CellKind.INVENTORY, custom.id()).stream().map(CellSources.Source::label).collect(Collectors.toList());
        assertEquals(List.of("Vorkath · Inventory", "Herbs · Right side"), inventories, "empty parts and the excluded setup are left out");

        CellSources.Source melee = CellSources.of(sections, CellKind.EQUIPMENT, null).get(1);
        assertTrue(melee.cell().name().isEmpty(), "a copied cell arrives unnamed so the target names it");
        assertEquals(CellKind.EQUIPMENT, melee.cell().kind());
    }
}
