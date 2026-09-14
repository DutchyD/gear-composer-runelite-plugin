package dev.dutchy.runelite.gear.content;

import dev.dutchy.runelite.libs.ui.item.ItemId;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DropRuleTest {

    private final DropRule rule = new DropRule(item -> item.equals(ItemId.of(4151)) ? Optional.of(EquipmentSlot.WEAPON) : Optional.empty());

    @Test
    void gridsTakeAnything() {
        assertTrue(rule.allows(SetupItem.of(4151), SlotRef.of(GridKind.INVENTORY, 0)));
    }

    @Test
    void equipmentOnlyTakesWhatIsWornThere() {
        assertTrue(rule.allows(SetupItem.of(4151), SlotRef.of(EquipmentSlot.WEAPON)));
        assertFalse(rule.allows(SetupItem.of(4151), SlotRef.of(EquipmentSlot.HEAD)));
    }

    @Test
    void unknownItemsAreGivenTheBenefitOfTheDoubt() {
        assertTrue(rule.allows(SetupItem.of(999), SlotRef.of(EquipmentSlot.HEAD)));
        assertTrue(new DropRule(EquipmentSlots.unknown()).allows(SetupItem.of(4151), SlotRef.of(EquipmentSlot.HEAD)));
    }
}
