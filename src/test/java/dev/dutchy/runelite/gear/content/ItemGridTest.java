package dev.dutchy.runelite.gear.content;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ItemGridTest {

    @Test
    void isFourAcrossAndSevenDown() {
        assertEquals(4, ItemGrid.COLUMNS);
        assertEquals(7, ItemGrid.ROWS);
        assertEquals(28, ItemGrid.SIZE);
    }

    @Test
    void setsAndClearsSlots() {
        ItemGrid grid = ItemGrid.EMPTY.withSlot(3, SetupItem.of(4151));
        assertEquals(Optional.of(SetupItem.of(4151)), grid.slot(3));
        assertEquals(1, grid.filledSlots());
        assertTrue(grid.clearSlot(3).isEmpty());
    }

    @Test
    void replacingASlotKeepsOneEntry() {
        ItemGrid grid = ItemGrid.EMPTY.withSlot(0, SetupItem.of(4151)).withSlot(0, SetupItem.of(995, 500));
        assertEquals(1, grid.filledSlots());
        assertEquals(500, grid.slot(0).orElseThrow().quantity().orElseThrow());
    }

    @Test
    void isImmutable() {
        ItemGrid original = ItemGrid.EMPTY.withSlot(0, SetupItem.of(4151));
        original.withSlot(1, SetupItem.of(995));
        assertEquals(1, original.filledSlots());
    }

    @Test
    void mapsIndexesToColumnsAndRows() {
        assertEquals(0, ItemGrid.column(0));
        assertEquals(0, ItemGrid.row(0));
        assertEquals(3, ItemGrid.column(7));
        assertEquals(1, ItemGrid.row(7));
        assertEquals(3, ItemGrid.row(12));
    }

    @Test
    void rejectsSlotsOutsideTheGrid() {
        assertThrows(IllegalArgumentException.class, () -> ItemGrid.EMPTY.slot(-1));
        assertThrows(IllegalArgumentException.class, () -> ItemGrid.EMPTY.slot(ItemGrid.SIZE));
        assertThrows(IllegalArgumentException.class, () -> ItemGrid.of(Map.of(99, SetupItem.of(1))));
    }

    @Test
    void clearingAnEmptySlotChangesNothing() {
        assertEquals(ItemGrid.EMPTY, ItemGrid.EMPTY.clearSlot(5));
    }
    @Test
    void dividersSitAboveRowsWithoutTakingSlotsAndShiftBankRows() {
        ItemGrid grid = ItemGrid.EMPTY.withSlot(0, SetupItem.of(385)).withDivider(0, "Food").withDivider(3, " Runes ");
        assertEquals("Food", grid.dividerAt(0, 0).orElseThrow().label());
        assertEquals("Runes", grid.dividerAt(3, 3).orElseThrow().label());
        assertTrue(grid.dividerAt(1, 0).isEmpty());
        assertTrue(grid.dividersAbove(0).get(0).spansWholeRow());
        assertEquals(1, grid.filledSlots());
        assertEquals(List.of(0, 3), List.copyOf(grid.rowsWithDividers()), "the rows that grow a header");
        assertEquals(grid.withoutDividersAbove(0).withoutDividersAbove(3), ItemGrid.EMPTY.withSlot(0, SetupItem.of(385)));
        assertThrows(IllegalArgumentException.class, () -> grid.withDivider(7, "x"));
        assertThrows(IllegalArgumentException.class, () -> grid.withDivider(1, "   "));
        assertThrows(IllegalArgumentException.class, () -> grid.withDivider(1, "x".repeat(ItemGrid.MAX_DIVIDER_LENGTH + 1)));
        assertNotEquals(grid, grid.withoutDividersAbove(0), "dividers count towards equality");
    }

    @Test
    void severalDividersShareARowAndANewOneTrimsWhatItOverlaps() {
        Divider food = new Divider(2, 0, 1, "Food", TextAlign.CENTRE);
        Divider pots = new Divider(2, 2, 3, "Pots", TextAlign.RIGHT);
        ItemGrid grid = ItemGrid.EMPTY.withDivider(food).withDivider(pots);

        assertEquals(List.of(food, pots), grid.dividersAbove(2));
        assertEquals(List.of(2), List.copyOf(grid.rowsWithDividers()), "dividers on one row share one header");
        assertEquals(Optional.of(pots), grid.dividerAt(2, 3));
        assertEquals(List.of(pots), grid.withoutDivider(food).dividers());

        ItemGrid runes = grid.withDivider(new Divider(2, 1, 2, "Runes", TextAlign.LEFT));
        assertEquals(List.of("Food", "Runes", "Pots"), runes.dividersAbove(2).stream().map(Divider::label).collect(Collectors.toList()));
        assertEquals(List.of(0, 1, 3), runes.dividersAbove(2).stream().map(Divider::fromColumn).collect(Collectors.toList()), "neighbours shrink to make room");
        assertEquals(List.of("Whole"), grid.withDivider(2, "Whole").dividersAbove(2).stream().map(Divider::label).collect(Collectors.toList()),
                "a divider across the row replaces the ones it covers");
        assertThrows(IllegalArgumentException.class, () -> ItemGrid.of(Map.of(), List.of(food, food.withLabel("Again"))), "overlaps are refused");
        assertThrows(IllegalArgumentException.class, () -> new Divider(0, 2, 1, "Backwards", TextAlign.LEFT));
        assertThrows(IllegalArgumentException.class, () -> new Divider(0, 0, ItemGrid.COLUMNS, "Wide", TextAlign.LEFT));
    }

}
