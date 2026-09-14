package dev.dutchy.runelite.gear.ledger;

import dev.dutchy.runelite.gear.content.*;
import dev.dutchy.runelite.libs.ui.item.ItemId;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LedgerTest {

    private static final EquipmentStats WHIP_STATS = new EquipmentStats(0, 82, 0, 0, 0, 0, 0, 0, 0, 0, 82, 0, 0, 0, 4);
    private static final EquipmentStats STAFF_STATS = new EquipmentStats(0, 0, 0, 20, 0, 0, 0, 0, 5, 0, 0, 0, 15, 0, 4);
    private static final ItemFactsSource FACTS = ItemFactsSource.fixed(Map.of(
            ItemId.of(4151), new ItemFacts(2_500_000, 0.4, WHIP_STATS),
            ItemId.of(11791), new ItemFacts(60_000_000, 2.2, STAFF_STATS),
            ItemId.of(385), ItemFacts.unworn(900, 0.5),
            ItemId.of(995), ItemFacts.unworn(1, 0)));

    @Test
    void sumsValueWeightAndWornBonusesQuantityAware() {
        GearContent gear = GearContent.empty()
                .withEquipped(EquipmentSlot.WEAPON, SetupItem.of(4151))
                .withInventory(ItemGrid.EMPTY.withSlot(0, SetupItem.of(385, 4)).withSlot(1, SetupItem.of(995, 100_000)));

        Ledger ledger = Ledger.of(gear, FACTS);

        assertEquals(2_500_000L + 4 * 900L + 100_000L, ledger.value());
        assertEquals(0.4 + 4 * 0.5, ledger.weight(), 0.0001);
        assertEquals(WHIP_STATS, ledger.stats());
        assertFalse(ledger.openEnded());
        assertEquals(3, ledger.itemCount());
        assertEquals(0, ledger.unknownItems());
    }

    @Test
    void bankAmountSlotsCountOnceAndUnknownItemsAreCounted() {
        BankContent bank = BankContent.empty()
                .withLeft(ItemGrid.EMPTY.withSlot(0, SetupItem.of(385)).withSlot(1, SetupItem.of(999_999)));

        Ledger ledger = Ledger.of(bank, FACTS);

        assertTrue(ledger.openEnded());
        assertEquals(900L, ledger.value());
        assertEquals(1, ledger.unknownItems());
        assertTrue(ledger.hasUnknownItems());
        assertEquals(EquipmentStats.ZERO, ledger.stats(), "inventory items add no bonuses");
    }

    @Test
    void equipmentLedgersOfTwoVariantsCanBeSubtracted() {
        GearContent melee = GearContent.empty().withEquipped(EquipmentSlot.WEAPON, SetupItem.of(4151));
        GearContent mage = melee.withEquipped(EquipmentSlot.WEAPON, SetupItem.of(11791));

        Ledger base = Ledger.ofEquipment(melee.equipment(), FACTS);
        Ledger swapped = Ledger.ofEquipment(mage.equipment(), FACTS);

        assertEquals(STAFF_STATS, swapped.stats());
        assertEquals(20, swapped.stats().minus(base.stats()).magicAttack());
        assertEquals(-82, swapped.stats().minus(base.stats()).slashAttack());
        assertEquals(60_000_000L - 2_500_000L, swapped.value() - base.value());
    }

    @Test
    void formatsReadLikeTheGame() {
        assertEquals("41.2M", LedgerFormat.coins(41_200_000));
        assertEquals("2.5B", LedgerFormat.coins(2_500_000_000L));
        assertEquals("12K", LedgerFormat.coins(12_000));
        assertEquals("999", LedgerFormat.coins(999));
        assertEquals("+65.3M", LedgerFormat.coinsDelta(65_300_000));
        assertEquals("-1.5M", LedgerFormat.coinsDelta(-1_500_000));
        assertEquals("18.4 kg", LedgerFormat.weight(18.42));
        assertEquals("+112", LedgerFormat.signed(112));
        assertEquals("-38", LedgerFormat.signed(-38));
        assertEquals("+0.4", LedgerFormat.weightDelta(0.4));
    }
    @Test
    void aCustomLayoutSumsValueAndWeightButNotBonusesAcrossCells() {
        CustomContent custom = CustomContent.empty(1)
                .withCell(CellRef.of(0, 0), LayoutCell.equipment(Map.of(EquipmentSlot.WEAPON, SetupItem.of(4151))))
                .withCell(CellRef.of(0, 1), LayoutCell.inventory(ItemGrid.EMPTY.withSlot(0, SetupItem.of(385, 2))));

        Ledger ledger = Ledger.of(custom, FACTS);

        assertEquals(2_500_000L + 2 * 900L, ledger.value());
        assertEquals(0.4 + 2 * 0.5, ledger.weight(), 0.0001);
        assertEquals(EquipmentStats.ZERO, ledger.stats(), "several sets do not add up");
        assertEquals(WHIP_STATS, Ledger.ofCell(custom.cell(CellRef.of(0, 0)), FACTS).stats());
    }

}
