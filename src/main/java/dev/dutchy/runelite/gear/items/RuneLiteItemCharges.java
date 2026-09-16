package dev.dutchy.runelite.gear.items;

import dev.dutchy.runelite.libs.ui.item.ItemId;
import net.runelite.api.Client;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.concurrent.ConcurrentHashMap;

/** Reads charge counts from item names; must be used on the client thread. */
@Singleton
public final class RuneLiteItemCharges implements ItemCharges {

    private final Client client;
    private final Map<ItemId, OptionalInt> known = new ConcurrentHashMap<>();

    @Inject
    public RuneLiteItemCharges(Client client) {
        this.client = Objects.requireNonNull(client, "client");
    }

    @Override
    public OptionalInt chargesOf(ItemId item) {
        return known.computeIfAbsent(Objects.requireNonNull(item, "item"), this::lookUp);
    }

    private OptionalInt lookUp(ItemId item) {
        return ItemCharges.parse(client.getItemDefinition(item.value()).getName());
    }
}
