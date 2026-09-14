package dev.dutchy.runelite.gear.bank;

import dev.dutchy.runelite.gear.GearSetup;
import dev.dutchy.runelite.gear.content.BankContent;
import dev.dutchy.runelite.gear.content.EquipmentSlot;
import dev.dutchy.runelite.gear.content.GearContent;
import dev.dutchy.runelite.gear.content.ItemGrid;
import dev.dutchy.runelite.gear.content.SetupItem;
import dev.dutchy.runelite.gear.content.SetupVariant;
import dev.dutchy.runelite.gear.layout.BankPlacement;
import dev.dutchy.runelite.gear.layout.BankSide;
import dev.dutchy.runelite.gear.layout.LayoutLabel;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BankLayoutPlannerTest {

    private static final SetupItem WHIP = SetupItem.of(4151);
    private static final SetupItem HELM = SetupItem.of(1163);
    private static final SetupItem COINS = SetupItem.of(995, 1000);

    private final BankLayoutPlanner planner = new BankLayoutPlanner();

    @Test
    void gearPutsEquipmentLeftAndInventoryRight() {
        GearContent gear = GearContent.empty()
                .withEquipped(EquipmentSlot.WEAPON, WHIP)
                .withEquipped(EquipmentSlot.HEAD, HELM)
                .withInventory(ItemGrid.EMPTY.withSlot(0, COINS));

        BankLayout layout = planner.plan(gear);

        assertEquals(2, layout.side(BankSide.LEFT).size());
        assertEquals(1, layout.side(BankSide.RIGHT).size());
        assertEquals(3, layout.size());
    }

    @Test
    void equipmentKeepsItsOnScreenArrangement() {
        GearContent gear = GearContent.empty().withEquipped(EquipmentSlot.HEAD, HELM);
        BankPlacement placement = planner.plan(gear).side(BankSide.LEFT).get(0);

        assertEquals(EquipmentSlot.HEAD.column(), placement.column());
        assertEquals(EquipmentSlot.HEAD.row(), placement.row());
        assertEquals(HELM, placement.item());
    }

    @Test
    void inventorySlotsKeepTheirGridPosition() {
        GearContent gear = GearContent.empty().withInventory(ItemGrid.EMPTY.withSlot(7, COINS));
        BankPlacement placement = planner.plan(gear).side(BankSide.RIGHT).get(0);

        assertEquals(3, placement.column());
        assertEquals(1, placement.row());
        assertEquals(COINS, placement.item());
    }

    @Test
    void generalSetupsPlanBothGrids() {
        BankContent general = BankContent.empty()
                .withLeft(ItemGrid.EMPTY.withSlot(0, WHIP))
                .withRight(ItemGrid.EMPTY.withSlot(1, COINS));

        BankLayout layout = planner.plan(general);

        assertEquals(List.of(WHIP), layout.side(BankSide.LEFT).stream().map(BankPlacement::item).collect(Collectors.toList()));
        assertEquals(List.of(COINS), layout.side(BankSide.RIGHT).stream().map(BankPlacement::item).collect(Collectors.toList()));
    }

    @Test
    void anEmptySetupPlansNothing() {
        assertTrue(planner.plan(GearContent.empty()).isEmpty());
        assertTrue(planner.plan(BankContent.empty()).isEmpty());
    }

    @Test
    void quantitiesSurvivePlanning() {
        GearContent gear = GearContent.empty().withInventory(ItemGrid.EMPTY.withSlot(0, COINS));
        assertEquals(1000, planner.plan(gear).side(BankSide.RIGHT).get(0).item().quantity().orElseThrow());
    }

    @Test
    void onlyTheChosenVariantOfASetupIsPlanned() {
        GearContent melee = GearContent.empty().withEquipped(EquipmentSlot.WEAPON, WHIP);
        GearContent mage = melee.withEquipped(EquipmentSlot.WEAPON, SetupItem.of(4587));
        GearSetup setup = GearSetup.named("Vorkath").withContent(melee)
                .withAddedVariant(new SetupVariant("Mage", mage)).withSelectedVariant(1);

        List<BankPlacement> placements = planner.plan(setup.content()).placements();

        assertEquals(1, placements.size());
        assertEquals(SetupItem.of(4587), placements.get(0).item());
        assertEquals(EquipmentSlot.WEAPON.row(), placements.get(0).row(), "a variant draws where the base gear does");
    }

    @Test
    void dividersHeadTheirRowAndBothSidesGrowTheHeaderTogether() {
        GearContent gear = GearContent.empty()
                .withEquipped(EquipmentSlot.HEAD, HELM)
                .withInventory(ItemGrid.EMPTY.withSlot(0, COINS).withSlot(27, WHIP).withDivider(0, "Food").withDivider(6, "Coins"));

        BankLayout layout = planner.plan(gear);

        assertEquals(EquipmentSlot.HEAD.row(), layout.side(BankSide.LEFT).get(0).row(), "the other side keeps its rows");
        assertEquals(0, layout.side(BankSide.RIGHT).get(0).row(), "a divider heads its row rather than pushing it down");
        assertEquals(6, layout.side(BankSide.RIGHT).get(1).row());
        assertEquals(Set.of(0, 6), layout.headerRows(), "both sides grow a header on those rows");
        List<LayoutLabel> labels = layout.labels();
        assertEquals(2, labels.size());
        assertEquals(BankSide.RIGHT, labels.get(0).side());
        assertEquals(0, labels.get(0).row());
        assertEquals("Food", labels.get(0).text());
        assertTrue(labels.get(0).header());
        assertEquals(6, labels.get(1).row());
        BankRows rows = layout.rows();
        assertEquals(BankGeometry.HEADER_HEIGHT, rows.itemY(0), "row 0's items sit under its header");
        assertEquals(36 + BankGeometry.HEADER_HEIGHT, rows.itemY(1));
        assertEquals(6 * 36 + BankGeometry.HEADER_HEIGHT, rows.top(6));
        assertEquals(6 * 36 + 2 * BankGeometry.HEADER_HEIGHT + 32, rows.height(7));
    }
}
