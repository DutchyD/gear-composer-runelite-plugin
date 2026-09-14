package dev.dutchy.runelite.gear.ledger;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LedgerRowsTest {

    @Test
    void laysTheBonusesOutLikeTheEquipmentScreen() {
        EquipmentStats stats = new EquipmentStats(0, 82, -5, 0, 0, 10, 0, 0, 0, 0, 82, 0, 2.5, 3, 4);
        List<LedgerRows.Row> rows = LedgerRows.of(stats);
        assertEquals(List.of("Attack", "Defence", "Other"), List.of(rows.get(0).label(), rows.get(1).label(), rows.get(2).label()));
        assertEquals(LedgerRows.STYLES.size(), rows.get(0).cells().size());
        assertEquals("+82", rows.get(0).cells().get(1).text());
        assertEquals(LedgerRows.Tone.UP, rows.get(0).cells().get(1).tone());
        assertEquals("-5", rows.get(0).cells().get(2).text());
        assertEquals(LedgerRows.Tone.DOWN, rows.get(0).cells().get(2).tone());
        assertEquals("0", rows.get(1).cells().get(1).text());
        assertEquals(LedgerRows.Tone.FLAT, rows.get(1).cells().get(1).tone());
        assertEquals("+2.5%", rows.get(2).cells().get(2).text());
        assertEquals("4", rows.get(2).cells().get(4).text());
        assertEquals("–", LedgerRows.of(EquipmentStats.ZERO).get(2).cells().get(4).text(), "no weapon means no speed");
        assertEquals("0%", LedgerRows.of(EquipmentStats.ZERO).get(2).cells().get(2).text());
    }
}
