package dev.dutchy.runelite.gear.bank;

import dev.dutchy.runelite.gear.layout.BankSide;
import dev.dutchy.runelite.gear.layout.BankPlacement;
import dev.dutchy.runelite.gear.content.EquipmentSlot;
import dev.dutchy.runelite.gear.content.GearContent;
import dev.dutchy.runelite.gear.content.BankContent;
import dev.dutchy.runelite.gear.content.ItemGrid;
import dev.dutchy.runelite.gear.content.SetupItem;
import dev.dutchy.runelite.gear.items.ItemVariants;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BankGridTest {

    private static final SetupItem WHIP = SetupItem.of(4151);
    private static final SetupItem COINS = SetupItem.of(995, 1000);

    private final BankGrid grid = new BankGrid(new SlotResolver(ItemVariants.none()));
    private final BankLayoutPlanner planner = new BankLayoutPlanner();

    @Test
    void theBankIsEightWideSplitDownTheMiddle() {
        assertEquals(8, BankGrid.ITEMS_PER_ROW);
        assertEquals(4, BankGrid.COLUMNS_PER_SIDE);
    }

    @Test
    void leftSidePlacementsTakeTheFirstFourColumns() {
        assertEquals(0, BankGrid.slotIndex(new BankPlacement(BankSide.LEFT, 0, 0, WHIP)));
        assertEquals(3, BankGrid.slotIndex(new BankPlacement(BankSide.LEFT, 3, 0, WHIP)));
        assertEquals(8, BankGrid.slotIndex(new BankPlacement(BankSide.LEFT, 0, 1, WHIP)));
    }

    @Test
    void rightSidePlacementsTakeTheLastFourColumns() {
        assertEquals(4, BankGrid.slotIndex(new BankPlacement(BankSide.RIGHT, 0, 0, WHIP)));
        assertEquals(7, BankGrid.slotIndex(new BankPlacement(BankSide.RIGHT, 3, 0, WHIP)));
        assertEquals(12, BankGrid.slotIndex(new BankPlacement(BankSide.RIGHT, 0, 1, WHIP)));
    }

    @Test
    void aGearSetupKeepsEquipmentLeftAndInventoryRight() {
        GearContent gear = GearContent.empty()
                .withEquipped(EquipmentSlot.HEAD, WHIP)
                .withInventory(ItemGrid.EMPTY.withSlot(0, COINS));

        Map<Integer, SetupItem> slots = grid.map(planner.plan(gear));

        assertEquals(WHIP, slots.get(1), "head sits in the middle of the three equipment columns");
        assertEquals(COINS, slots.get(4), "the inventory starts in the fifth column");
    }

    @Test
    void aBankSetupFillsBothHalvesOfEachRow() {
        BankContent content = BankContent.empty()
                .withLeft(ItemGrid.EMPTY.withSlot(3, WHIP))
                .withRight(ItemGrid.EMPTY.withSlot(0, COINS));

        Map<Integer, SetupItem> slots = grid.map(planner.plan(content));

        assertEquals(WHIP, slots.get(3));
        assertEquals(COINS, slots.get(4));
    }

    @Test
    void countsTheRowsARowNeeds() {
        assertEquals(0, grid.rowsNeeded(BankLayout.EMPTY));
        assertEquals(7, grid.rowsNeeded(planner.plan(
                GearContent.empty().withInventory(ItemGrid.EMPTY.withSlot(ItemGrid.SIZE - 1, WHIP)))));
    }

    @Test
    void aSideCannotBeWiderThanFourColumns() {
        assertThrows(IllegalArgumentException.class,
                () -> BankGrid.slotIndex(new BankPlacement(BankSide.LEFT, 4, 0, WHIP)));
    }

    @Test
    void slotsAreMarkedAsPlaceholdersWhenTheBankLacksTheItem() {
        BankContent content = BankContent.empty()
                .withLeft(ItemGrid.EMPTY.withSlot(0, WHIP))
                .withRight(ItemGrid.EMPTY.withSlot(0, COINS));
        BankContents onlyWhip = item -> item.equals(WHIP.id());

        var plan = grid.plan(planner.plan(content), onlyWhip);

        assertEquals(2, plan.size());
        assertEquals(0, plan.get(0).slotIndex());
        assertFalse(plan.get(0).placeholder(), "the whip is banked");
        assertEquals(4, plan.get(1).slotIndex());
        assertTrue(plan.get(1).placeholder(), "the coins are not banked");
    }

    @Test
    void anEmptyBankMakesEverytingAPlaceholder() {
        BankContent content = BankContent.empty().withLeft(ItemGrid.EMPTY.withSlot(0, WHIP));
        assertTrue(grid.plan(planner.plan(content), BankContents.nothing()).get(0).placeholder());
    }

    @Test
    void plannedSlotsComeBackInDisplayOrder() {
        BankContent content = BankContent.empty()
                .withRight(ItemGrid.EMPTY.withSlot(0, COINS))
                .withLeft(ItemGrid.EMPTY.withSlot(2, WHIP));

        var plan = grid.plan(planner.plan(content), BankContents.nothing());

        assertEquals(2, plan.get(0).slotIndex());
        assertEquals(4, plan.get(1).slotIndex());
    }

    @Test
    void everyLayoutSlotIsDrawnWhateverTheBankHolds() {
        BankContent content = BankContent.empty()
                .withLeft(ItemGrid.EMPTY.withSlot(0, WHIP).withSlot(1, COINS))
                .withRight(ItemGrid.EMPTY.withSlot(0, SetupItem.of(1163)));
        BankLayout layout = planner.plan(content);

        assertEquals(layout.size(), grid.plan(layout, BankContents.nothing()).size(),
                "an empty bank still shows the whole layout");
        assertEquals(layout.size(), grid.plan(layout, item -> true).size(),
                "a full bank shows the whole layout too");
    }

    @Test
    void aSlotBecomesAPlaceholderOnceTheItemLeavesTheBank() {
        BankContent content = BankContent.empty().withLeft(ItemGrid.EMPTY.withSlot(0, WHIP));
        BankLayout layout = planner.plan(content);

        BankContents stocked = item -> item.equals(WHIP.id());
        assertFalse(grid.plan(layout, stocked).get(0).placeholder());

        assertTrue(grid.plan(layout, BankContents.nothing()).get(0).placeholder(),
                "withdrawing the last one leaves a placeholder, not a gap");
    }

    @Test
    void theSamePositionsAreUsedWhetherOrNotTheItemIsBanked() {
        BankContent content = BankContent.empty().withRight(ItemGrid.EMPTY.withSlot(5, COINS));
        BankLayout layout = planner.plan(content);

        assertEquals(grid.plan(layout, item -> true).get(0).slotIndex(),
                grid.plan(layout, BankContents.nothing()).get(0).slotIndex());
    }

    @Test
    void anEmptyLayoutMapsToNothing() {
        assertTrue(grid.map(BankLayout.EMPTY).isEmpty());
    }

    @Test
    void everyPlacementOfAnOwnedItemIsPlannedAsOwned() {
        BankContent content = BankContent.empty()
                .withLeft(ItemGrid.EMPTY.withSlot(0, COINS).withSlot(1, COINS))
                .withRight(ItemGrid.EMPTY.withSlot(0, COINS));

        var plan = grid.plan(planner.plan(content), item -> item.equals(COINS.id()));

        assertEquals(3, plan.size());
        assertTrue(plan.stream().noneMatch(BankSlotPlan::placeholder), "a repeated item is never a placeholder");
    }
}
