package dev.dutchy.runelite.gear.content;

import dev.dutchy.runelite.libs.ui.item.ItemId;

import javax.inject.Inject;
import java.util.Collection;
import java.util.Objects;

/** Whether an item may land in a slot: grids take anything, equipment only what is worn there. */
public final class DropRule {

    private final EquipmentSlots equipmentSlots;

    @Inject
    public DropRule(EquipmentSlots equipmentSlots) {
        this.equipmentSlots = Objects.requireNonNull(equipmentSlots, "equipmentSlots");
    }

    public boolean allows(SetupItem item, SlotRef target) {
        Objects.requireNonNull(item, "item");
        Objects.requireNonNull(target, "target");
        SlotRef slot = target instanceof SlotRef.Cell ? ((SlotRef.Cell) target).inner() : target;
        if (!(slot instanceof SlotRef.Equipment)) {
            return true;
        }
        EquipmentSlot wornIn = ((SlotRef.Equipment) slot).slot();
        return equipmentSlots.slotOf(item.id()).map(wornIn::equals).orElse(true);
    }

    public void warmUp(Collection<ItemId> items) {
        equipmentSlots.warmUp(Objects.requireNonNull(items, "items"));
    }
}
