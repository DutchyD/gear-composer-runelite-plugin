package dev.dutchy.runelite.gear.content;

import lombok.Value;

import java.util.*;

/** Worn equipment and inventory. */
@Value
public class GearContent implements SetupContent {
    Map<EquipmentSlot, SetupItem> equipment;
    ItemGrid inventory;

    public GearContent(Map<EquipmentSlot, SetupItem> equipment, ItemGrid inventory) {
        Objects.requireNonNull(equipment, "equipment");
        Objects.requireNonNull(inventory, "inventory");
        this.equipment = Collections.unmodifiableMap(copy(equipment));
        this.inventory = inventory;
    }

    public static GearContent empty() {
        return new GearContent(Map.of(), ItemGrid.EMPTY);
    }

    @Override
    public SetupType type() {
        return SetupType.GEAR;
    }

    @Override
    public boolean isEmpty() {
        return equipment.isEmpty() && inventory.isEmpty();
    }

    public Optional<SetupItem> equipped(EquipmentSlot slot) {
        Objects.requireNonNull(slot, "slot");
        return Optional.ofNullable(equipment.get(slot));
    }

    public GearContent withEquipped(EquipmentSlot slot, SetupItem item) {
        Objects.requireNonNull(slot, "slot");
        Objects.requireNonNull(item, "item");
        Map<EquipmentSlot, SetupItem> updated = copy(equipment);
        updated.put(slot, item);
        return new GearContent(updated, inventory);
    }

    public GearContent withoutEquipped(EquipmentSlot slot) {
        Objects.requireNonNull(slot, "slot");
        Map<EquipmentSlot, SetupItem> updated = copy(equipment);
        updated.remove(slot);
        return new GearContent(updated, inventory);
    }

    public GearContent withInventory(ItemGrid newInventory) {
        return new GearContent(equipment, newInventory);
    }

    private static Map<EquipmentSlot, SetupItem> copy(Map<EquipmentSlot, SetupItem> source) {
        Map<EquipmentSlot, SetupItem> copy = new EnumMap<>(EquipmentSlot.class);
        copy.putAll(source);
        return copy;
    }
}
