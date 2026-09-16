package dev.dutchy.runelite.gear.content;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Value;

import java.util.*;

/** One cell of a custom layout: an equipment set, an inventory, or nothing, with an optional name. */
@Value
public class LayoutCell {
    CellKind kind;
    @Getter(AccessLevel.NONE)
    String name;
    Map<EquipmentSlot, SetupItem> equipment;
    ItemGrid inventory;

    public static final int MAX_NAME_LENGTH = 24;
    public static final LayoutCell EMPTY = new LayoutCell(CellKind.EMPTY, null, Map.of(), ItemGrid.EMPTY);

    public LayoutCell(CellKind kind, String name, Map<EquipmentSlot, SetupItem> equipment, ItemGrid inventory) {
        this.kind = Objects.requireNonNull(kind, "kind");
        this.name = name == null ? null : requireValidName(name);
        Objects.requireNonNull(equipment, "equipment");
        this.inventory = Objects.requireNonNull(inventory, "inventory");
        if (kind != CellKind.EQUIPMENT && !equipment.isEmpty()) {
            throw new IllegalArgumentException("Only an equipment cell holds equipment");
        }
        if (kind != CellKind.INVENTORY && !inventory.isEmpty()) {
            throw new IllegalArgumentException("Only an inventory cell holds an inventory");
        }
        Map<EquipmentSlot, SetupItem> copy = new EnumMap<>(EquipmentSlot.class);
        copy.putAll(equipment);
        this.equipment = Collections.unmodifiableMap(copy);
    }

    public static LayoutCell equipment(Map<EquipmentSlot, SetupItem> equipment) {
        return new LayoutCell(CellKind.EQUIPMENT, null, equipment, ItemGrid.EMPTY);
    }

    public static LayoutCell inventory(ItemGrid inventory) {
        return new LayoutCell(CellKind.INVENTORY, null, Map.of(), inventory);
    }

    public Optional<String> name() {
        return Optional.ofNullable(name);
    }

    /** The name, or the kind when there is none. */
    public String label() {
        return name != null ? name : kind.displayName();
    }

    public boolean isEmpty() {
        return equipment.isEmpty() && inventory.isEmpty();
    }

    public boolean isBlank() {
        return kind == CellKind.EMPTY;
    }

    public LayoutCell withName(String newName) {
        return new LayoutCell(kind, Objects.requireNonNull(newName, "newName"), equipment, inventory);
    }

    /** The same name with the items of a fresh cell of the new kind. */
    public LayoutCell withKind(CellKind newKind) {
        return new LayoutCell(newKind, name, Map.of(), ItemGrid.EMPTY);
    }

    public LayoutCell withEquipment(Map<EquipmentSlot, SetupItem> newEquipment) {
        return new LayoutCell(CellKind.EQUIPMENT, name, newEquipment, ItemGrid.EMPTY);
    }

    public LayoutCell withInventory(ItemGrid newInventory) {
        return new LayoutCell(CellKind.INVENTORY, name, Map.of(), newInventory);
    }

    public LayoutCell cleared() {
        return new LayoutCell(kind, name, Map.of(), ItemGrid.EMPTY);
    }

    /** Whether a slot reference names a slot this cell has. */
    public boolean supports(SlotRef inner) {
        if (inner instanceof SlotRef.Equipment) {
            return kind == CellKind.EQUIPMENT;
        }
        return inner instanceof SlotRef.Grid && kind == CellKind.INVENTORY;
    }

    public Optional<SetupItem> item(SlotRef inner) {
        if (!supports(inner)) {
            return Optional.empty();
        }
        if (inner instanceof SlotRef.Equipment) {
            return Optional.ofNullable(equipment.get(((SlotRef.Equipment) inner).slot()));
        }
        return inventory.slot(((SlotRef.Grid) inner).index());
    }

    public LayoutCell withItem(SlotRef inner, SetupItem item) {
        Objects.requireNonNull(item, "item");
        if (!supports(inner)) {
            return this;
        }
        if (inner instanceof SlotRef.Equipment) {
            Map<EquipmentSlot, SetupItem> updated = new EnumMap<>(EquipmentSlot.class);
            updated.putAll(equipment);
            updated.put(((SlotRef.Equipment) inner).slot(), item);
            return withEquipment(updated);
        }
        return withInventory(inventory.withSlot(((SlotRef.Grid) inner).index(), item));
    }

    public LayoutCell withoutItem(SlotRef inner) {
        if (!supports(inner)) {
            return this;
        }
        if (inner instanceof SlotRef.Equipment) {
            Map<EquipmentSlot, SetupItem> updated = new EnumMap<>(EquipmentSlot.class);
            updated.putAll(equipment);
            updated.remove(((SlotRef.Equipment) inner).slot());
            return withEquipment(updated);
        }
        return withInventory(inventory.clearSlot(((SlotRef.Grid) inner).index()));
    }

    /** Every item in slot order. */
    public List<SetupItem> items() {
        List<SetupItem> items = new ArrayList<>();
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            SetupItem item = equipment.get(slot);
            if (item != null) {
                items.add(item);
            }
        }
        for (int index = 0; index < ItemGrid.SIZE; index++) {
            inventory.slot(index).ifPresent(items::add);
        }
        return List.copyOf(items);
    }

    public static String requireValidName(String name) {
        Objects.requireNonNull(name, "name");
        String trimmed = name.strip();
        if (trimmed.isEmpty() || trimmed.length() > MAX_NAME_LENGTH) {
            throw new IllegalArgumentException("Cell name must be 1 to " + MAX_NAME_LENGTH + " characters");
        }
        return trimmed;
    }

    public static boolean isValidName(String name) {
        return name != null && !name.isBlank() && name.strip().length() <= MAX_NAME_LENGTH;
    }
}
