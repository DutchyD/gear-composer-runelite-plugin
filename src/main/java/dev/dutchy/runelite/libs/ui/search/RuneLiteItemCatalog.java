package dev.dutchy.runelite.libs.ui.search;

import dev.dutchy.runelite.libs.ui.item.ResolvedItem;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.ItemComposition;
import net.runelite.client.callback.ClientThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Every item the game defines, not just the tradeable ones, so untradeables such as Barrows gloves
 * can be found by name.
 *
 * <p>Item definitions may only be read on the client thread, so the catalogue is built there a slice
 * at a time to avoid stalling the game, and {@link #items()} returns an empty list until it is done.
 */
@Singleton
public final class RuneLiteItemCatalog implements ItemCatalog {

    private static final Logger log = LoggerFactory.getLogger(RuneLiteItemCatalog.class);

    private static final int ITEMS_PER_SLICE = 1_000;
    private static final Duration READY_TIMEOUT = Duration.ofMinutes(2);

    private final Client client;
    private final ClientThread clientThread;
    private final AtomicBoolean building = new AtomicBoolean();

    private volatile List<ResolvedItem> cached = List.of();

    @Inject
    public RuneLiteItemCatalog(Client client, ClientThread clientThread) {
        this.client = Objects.requireNonNull(client, "client");
        this.clientThread = Objects.requireNonNull(clientThread, "clientThread");
    }

    @Override
    public List<ResolvedItem> items() {
        List<ResolvedItem> snapshot = cached;
        if (snapshot.isEmpty()) {
            startBuild();
        }
        return snapshot;
    }

    private void startBuild() {
        if (!building.compareAndSet(false, true)) {
            return;
        }
        List<ResolvedItem> collected = new ArrayList<>();
        int[] cursor = {0};
        long deadline = System.nanoTime() + READY_TIMEOUT.toNanos();

        clientThread.invoke(() -> {
            if (client.getGameState().ordinal() < GameState.LOGIN_SCREEN.ordinal()) {
                if (System.nanoTime() < deadline) {
                    return false;
                }
                log.debug("Gave up building the item catalogue: the game never became ready");
                building.set(false);
                return true;
            }

            int total = client.getItemCount();
            int end = Math.min(cursor[0] + ITEMS_PER_SLICE, total);
            for (; cursor[0] < end; cursor[0]++) {
                collect(collected, cursor[0]);
            }
            if (cursor[0] < total) {
                return false;
            }

            cached = List.copyOf(collected);
            building.set(false);
            log.debug("Catalogued {} of {} item definitions", collected.size(), total);
            return true;
        });
    }

    private void collect(List<ResolvedItem> into, int itemId) {
        ItemComposition definition = client.getItemDefinition(itemId);
        String name = definition.getName();
        if (ItemDefinitions.isBankable(name, definition.getNote(), definition.getPlaceholderTemplateId(),
                definition.getPlaceholderId())) {
            into.add(ResolvedItem.of(itemId, name));
        }
    }
}
