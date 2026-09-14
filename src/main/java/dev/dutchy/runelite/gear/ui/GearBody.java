package dev.dutchy.runelite.gear.ui;

import dev.dutchy.runelite.gear.content.GearContent;
import dev.dutchy.runelite.gear.content.GridKind;
import dev.dutchy.runelite.gear.content.SyncScope;
import dev.dutchy.runelite.gear.guide.HelpTopic;
import dev.dutchy.runelite.libs.ui.icon.ItemIconFactory;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** The equipment cross with the inventory below it. */
final class GearBody extends ContentBody {

    private final ContentActions actions;
    private final List<SlotView> equipmentSlots;
    private final List<SlotView> inventorySlots;

    GearBody(GearContent gear, ItemIconFactory icons, EquipmentSlotArtwork artwork, SlotView.Listener listener, ContentActions actions) {
        this.actions = actions;
        EquipmentView equipment = new EquipmentView(gear, icons, artwork, listener);
        equipmentSlots = equipment.slots();
        add(heading("Equipment", null));
        add(Help.anchor(centered(equipment), HelpTopic.EQUIPMENT));
        ItemGridView inventory = new ItemGridView(GridKind.INVENTORY, gear.inventory(), icons, listener);
        inventorySlots = inventory.slots();
        add(Box.createVerticalStrut(Ui.GAP));
        add(heading("Inventory", null));
        add(Help.anchor(centered(inventory), HelpTopic.INVENTORY));
    }

    @Override
    List<SlotView> slots() {
        List<SlotView> slots = new ArrayList<>(equipmentSlots);
        slots.addAll(inventorySlots);
        return slots;
    }

    @Override
    void show(ContentViewState view) {
    }

    @Override
    ContentViewState remember(ContentViewState view) {
        return view;
    }

    @Override
    Optional<JComponent> toolbarAction() {
        return Optional.of(syncButton(SetupContentPanel.SYNC_GEAR, "Replace every slot with what you are wearing and carrying",
                () -> actions.sync(SyncScope.GEAR)));
    }
}
