package dev.dutchy.runelite.gear.player;

import dev.dutchy.runelite.gear.content.EquipmentSlot;
import dev.dutchy.runelite.gear.content.EquipmentSlots;
import dev.dutchy.runelite.libs.ui.item.ItemId;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.ItemStats;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Reads worn slots from RuneLite's item statistics. The lookup needs the client thread, so answers
 * are cached and an unknown item is looked up in the background and reported as unknown meanwhile.
 */
@Singleton
public final class RuneLiteEquipmentSlots implements EquipmentSlots {

    private final ItemManager itemManager;
    private final ClientThread clientThread;
    private final Map<ItemId, Optional<EquipmentSlot>> known = new ConcurrentHashMap<>();

    @Inject
    public RuneLiteEquipmentSlots(ItemManager itemManager, ClientThread clientThread) {
        this.itemManager = Objects.requireNonNull(itemManager, "itemManager");
        this.clientThread = Objects.requireNonNull(clientThread, "clientThread");
    }

    @Override
    public Optional<EquipmentSlot> slotOf(ItemId item) {
        Objects.requireNonNull(item, "item");
        Optional<EquipmentSlot> cached = known.get(item);
        if (cached != null) {
            return cached;
        }
        warmUp(List.of(item));
        return Optional.empty();
    }

    @Override
    public void warmUp(Collection<ItemId> items) {
        List<ItemId> missing = items.stream().filter(item -> !known.containsKey(item)).distinct().collect(Collectors.toList());
        if (missing.isEmpty()) {
            return;
        }
        clientThread.invoke(() -> missing.forEach(item -> known.put(item, lookUp(item))));
    }

    /** Must run on the client thread. */
    private Optional<EquipmentSlot> lookUp(ItemId item) {
        ItemStats stats = itemManager.getItemStats(item.value());
        if (stats == null || !stats.isEquipable() || stats.getEquipment() == null) {
            return Optional.empty();
        }
        return WornSlots.fromIndex(stats.getEquipment().getSlot());
    }
}
