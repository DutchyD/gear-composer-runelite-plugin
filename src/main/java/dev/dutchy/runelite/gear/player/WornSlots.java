package dev.dutchy.runelite.gear.player;

import dev.dutchy.runelite.gear.content.EquipmentSlot;
import net.runelite.api.EquipmentInventorySlot;

import java.util.Map;
import java.util.Optional;

/** How the game's worn-equipment container indices line up with the plugin's equipment slots. */
public final class WornSlots {

    private static final Map<EquipmentSlot, EquipmentInventorySlot> BY_SLOT = Map.ofEntries(
            Map.entry(EquipmentSlot.HEAD, EquipmentInventorySlot.HEAD),
            Map.entry(EquipmentSlot.CAPE, EquipmentInventorySlot.CAPE),
            Map.entry(EquipmentSlot.AMULET, EquipmentInventorySlot.AMULET),
            Map.entry(EquipmentSlot.AMMUNITION, EquipmentInventorySlot.AMMO),
            Map.entry(EquipmentSlot.WEAPON, EquipmentInventorySlot.WEAPON),
            Map.entry(EquipmentSlot.BODY, EquipmentInventorySlot.BODY),
            Map.entry(EquipmentSlot.SHIELD, EquipmentInventorySlot.SHIELD),
            Map.entry(EquipmentSlot.LEGS, EquipmentInventorySlot.LEGS),
            Map.entry(EquipmentSlot.HANDS, EquipmentInventorySlot.GLOVES),
            Map.entry(EquipmentSlot.FEET, EquipmentInventorySlot.BOOTS),
            Map.entry(EquipmentSlot.RING, EquipmentInventorySlot.RING));

    private WornSlots() {
    }

    public static int indexOf(EquipmentSlot slot) {
        return BY_SLOT.get(slot).getSlotIdx();
    }

    public static Optional<EquipmentSlot> fromIndex(int index) {
        return BY_SLOT.entrySet().stream()
                .filter(entry -> entry.getValue().getSlotIdx() == index)
                .map(Map.Entry::getKey)
                .findFirst();
    }
}
