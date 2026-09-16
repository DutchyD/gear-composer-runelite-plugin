package dev.dutchy.runelite.gear.layout;

import dev.dutchy.runelite.gear.content.EquipmentSlot;
import dev.dutchy.runelite.gear.content.SetupItem;
import dev.dutchy.runelite.gear.content.SlotRef;

import java.util.*;
import java.util.function.UnaryOperator;

/** The worn-equipment cross: three columns by five rows in the in-game arrangement. */
public final class EquipmentBlock extends LayoutBlock {

    private final Map<EquipmentSlot, SetupItem> equipment;
    private final UnaryOperator<SlotRef> refs;
    private final boolean wornBonuses;

    EquipmentBlock(String title, int band, BankSide side, int firstRow, Integer labelRow,
                   Map<EquipmentSlot, SetupItem> equipment, UnaryOperator<SlotRef> refs, boolean wornBonuses) {
        super(title, band, side, firstRow, labelRow);
        Map<EquipmentSlot, SetupItem> copy = new EnumMap<>(EquipmentSlot.class);
        copy.putAll(Objects.requireNonNull(equipment, "equipment"));
        this.equipment = Collections.unmodifiableMap(copy);
        this.refs = Objects.requireNonNull(refs, "refs");
        this.wornBonuses = wornBonuses;
    }

    public Map<EquipmentSlot, SetupItem> equipment() {
        return equipment;
    }

    public SlotRef ref(EquipmentSlot slot) {
        return refs.apply(SlotRef.of(slot));
    }

    /** Whether these bonuses are the setup's own; several sets in one layout do not add up. */
    public boolean countsWornBonuses() {
        return wornBonuses;
    }

    @Override
    public int rowsTall() {
        return EquipmentSlot.ROWS;
    }

    @Override
    public int columnsWide() {
        return EquipmentSlot.COLUMNS;
    }

    @Override
    public List<Slot> slots() {
        List<Slot> slots = new ArrayList<>();
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            slots.add(new Slot(ref(slot), equipment.get(slot)));
        }
        return List.copyOf(slots);
    }

    @Override
    public List<BankPlacement> placements() {
        List<BankPlacement> placements = new ArrayList<>();
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            SetupItem item = equipment.get(slot);
            if (item != null) {
                placements.add(new BankPlacement(side(), slot.column(), firstRow() + slot.row(), item));
            }
        }
        return List.copyOf(placements);
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.equipment(this);
    }
}
