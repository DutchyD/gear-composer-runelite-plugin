package dev.dutchy.runelite.gear.player;

import dev.dutchy.runelite.gear.content.EquipmentSlot;
import dev.dutchy.runelite.gear.content.ItemGrid;
import dev.dutchy.runelite.gear.content.Loadout;
import dev.dutchy.runelite.gear.content.SetupItem;
import dev.dutchy.runelite.gear.items.ItemNotes;
import dev.dutchy.runelite.libs.ui.item.ItemId;
import dev.dutchy.runelite.libs.ui.swing.EdtDispatch;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.gameval.InventoryID;
import net.runelite.client.callback.ClientThread;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;
import java.util.function.Consumer;

@Singleton
public final class RuneLitePlayerItems implements PlayerItems {

    private final Client client;
    private final ClientThread clientThread;
    private final ItemNotes notes;

    @Inject
    public RuneLitePlayerItems(Client client, ClientThread clientThread, ItemNotes notes) {
        this.client = Objects.requireNonNull(client, "client");
        this.clientThread = Objects.requireNonNull(clientThread, "clientThread");
        this.notes = Objects.requireNonNull(notes, "notes");
    }

    @Override
    public void capture(Consumer<Loadout> onCaptured, Runnable onUnavailable) {
        Objects.requireNonNull(onCaptured, "onCaptured");
        Objects.requireNonNull(onUnavailable, "onUnavailable");
        clientThread.invoke(() -> {
            Optional<Loadout> loadout = read();
            EdtDispatch.onEdt(() -> loadout.ifPresentOrElse(onCaptured, onUnavailable));
        });
    }

    private Optional<Loadout> read() {
        if (client.getGameState() != GameState.LOGGED_IN) {
            return Optional.empty();
        }
        return Optional.of(new Loadout(
                equipment(client.getItemContainer(InventoryID.WORN)),
                inventory(client.getItemContainer(InventoryID.INV))));
    }

    private Map<EquipmentSlot, SetupItem> equipment(ItemContainer worn) {
        Map<EquipmentSlot, SetupItem> equipment = new EnumMap<>(EquipmentSlot.class);
        if (worn == null) {
            return equipment;
        }
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            setupItem(worn.getItem(WornSlots.indexOf(slot))).ifPresent(item -> equipment.put(slot, item));
        }
        return equipment;
    }

    private ItemGrid inventory(ItemContainer carried) {
        Map<Integer, SetupItem> slots = new LinkedHashMap<>();
        if (carried == null) {
            return ItemGrid.EMPTY;
        }
        for (int index = 0; index < ItemGrid.SIZE; index++) {
            int slotIndex = index;
            setupItem(carried.getItem(index)).ifPresent(item -> slots.put(slotIndex, item));
        }
        return ItemGrid.of(slots);
    }

    /** A carried note is recorded as the real item, flagged to be withdrawn as a note. */
    private Optional<SetupItem> setupItem(Item item) {
        if (item == null || item.getId() < 0 || item.getQuantity() < SetupItem.MIN_QUANTITY) {
            return Optional.empty();
        }
        ItemId id = ItemId.of(item.getId());
        Optional<ItemId> unnoted = notes.unnotedOf(id);
        SetupItem setupItem = SetupItem.of(unnoted.orElse(id).value(), item.getQuantity());
        return Optional.of(unnoted.isPresent() ? setupItem.withNoted(true) : setupItem);
    }
}
