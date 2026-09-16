package dev.dutchy.runelite.gear.content;

import lombok.Value;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

/** What the player is wearing and carrying at one moment. */
@Value
public class Loadout {
    Map<EquipmentSlot, SetupItem> equipment;
    ItemGrid inventory;

    public Loadout(Map<EquipmentSlot, SetupItem> equipment, ItemGrid inventory) {
        Objects.requireNonNull(equipment, "equipment");
        Objects.requireNonNull(inventory, "inventory");
        Map<EquipmentSlot, SetupItem> copy = new EnumMap<>(EquipmentSlot.class);
        copy.putAll(equipment);
        equipment = Collections.unmodifiableMap(copy);
        this.equipment = equipment;
        this.inventory = inventory;
    }

    public static Loadout empty() {
        return new Loadout(Map.of(), ItemGrid.EMPTY);
    }
}
