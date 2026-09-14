package dev.dutchy.runelite.gear.player;

import dev.dutchy.runelite.gear.ledger.EquipmentStats;
import dev.dutchy.runelite.gear.ledger.ItemFacts;
import dev.dutchy.runelite.gear.ledger.ItemFactsSource;
import dev.dutchy.runelite.libs.ui.item.ItemId;
import dev.dutchy.runelite.libs.ui.swing.EdtDispatch;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.game.ItemEquipmentStats;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.ItemStats;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/** Prices and stats need the client thread, so answers are cached and listeners hear when a batch lands. */
@Singleton
public final class RuneLiteItemFacts implements ItemFactsSource {

    private final ItemManager itemManager;
    private final ClientThread clientThread;
    private final Map<ItemId, Optional<ItemFacts>> known = new ConcurrentHashMap<>();
    private final List<Runnable> listeners = new CopyOnWriteArrayList<>();

    @Inject
    public RuneLiteItemFacts(ItemManager itemManager, ClientThread clientThread) {
        this.itemManager = Objects.requireNonNull(itemManager, "itemManager");
        this.clientThread = Objects.requireNonNull(clientThread, "clientThread");
    }

    @Override
    public Optional<ItemFacts> factsOf(ItemId item) {
        Objects.requireNonNull(item, "item");
        Optional<ItemFacts> cached = known.get(item);
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
        clientThread.invoke(() -> {
            missing.forEach(item -> known.put(item, lookUp(item)));
            EdtDispatch.onEdt(() -> listeners.forEach(Runnable::run));
        });
    }

    @Override
    public void addListener(Runnable listener) {
        listeners.add(Objects.requireNonNull(listener, "listener"));
    }

    /** Must run on the client thread. */
    private Optional<ItemFacts> lookUp(ItemId item) {
        ItemStats stats = itemManager.getItemStats(item.value());
        if (stats == null) {
            return Optional.empty();
        }
        int price = Math.max(0, itemManager.getItemPrice(item.value()));
        ItemEquipmentStats worn = stats.isEquipable() ? stats.getEquipment() : null;
        return Optional.of(new ItemFacts(price, stats.getWeight(), worn == null ? null : bonuses(worn)));
    }

    private static EquipmentStats bonuses(ItemEquipmentStats worn) {
        return new EquipmentStats(worn.getAstab(), worn.getAslash(), worn.getAcrush(), worn.getAmagic(), worn.getArange(),
                worn.getDstab(), worn.getDslash(), worn.getDcrush(), worn.getDmagic(), worn.getDrange(),
                worn.getStr(), worn.getRstr(), worn.getMdmg(), worn.getPrayer(), worn.getAspeed());
    }
}
