package dev.dutchy.runelite.gear.bank;

import java.util.Set;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BankGeometryTest {

    @Test
    void theFirstSlotSitsAtTheStartOfTheFirstRow() {
        assertEquals(BankGeometry.ITEM_START_X, BankGeometry.x(0));
        assertEquals(0, BankGeometry.y(0));
    }

    @Test
    void columnsAdvanceByItemWidthPlusPadding() {
        assertEquals(BankGeometry.ITEM_START_X + 48, BankGeometry.x(1));
        assertEquals(BankGeometry.ITEM_START_X + 48 * 7, BankGeometry.x(7));
    }

    @Test
    void rowsWithHeadersGrowAndPushTheRowsBelowThemDown() {
        BankRows rows = new BankRows(Set.of(1));
        assertEquals(0, rows.itemY(0));
        assertEquals(36, rows.top(1), "the header sits where the row would have started");
        assertEquals(36 + BankGeometry.HEADER_HEIGHT, rows.itemY(1));
        assertEquals(72 + BankGeometry.HEADER_HEIGHT, rows.itemY(2));
        assertEquals(32, rows.height(1));
        assertEquals(72 + BankGeometry.HEADER_HEIGHT + 32, rows.height(3));
        assertEquals(0, rows.height(0));
        assertEquals(72, BankRows.plain().itemY(2), "without headers the rows keep the game's pitch");
    }

    @Test
    void rowsAdvanceByItemHeightPlusPadding() {
        assertEquals(BankGeometry.ITEM_START_X, BankGeometry.x(8), "a new row starts back at the left");
        assertEquals(36, BankGeometry.y(8));
        assertEquals(72, BankGeometry.y(16));
    }
}
