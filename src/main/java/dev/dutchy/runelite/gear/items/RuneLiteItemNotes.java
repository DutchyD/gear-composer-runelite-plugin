package dev.dutchy.runelite.gear.items;

import dev.dutchy.runelite.libs.ui.item.ItemId;
import net.runelite.api.Client;
import net.runelite.api.ItemComposition;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Objects;
import java.util.Optional;

/** Reads note links from item definitions; must be used on the client thread. */
@Singleton
public final class RuneLiteItemNotes implements ItemNotes {

    private final Client client;

    @Inject
    public RuneLiteItemNotes(Client client) {
        this.client = Objects.requireNonNull(client, "client");
    }

    @Override
    public Optional<ItemId> unnotedOf(ItemId item) {
        ItemComposition definition = client.getItemDefinition(Objects.requireNonNull(item, "item").value());
        if (definition.getNote() == -1) {
            return Optional.empty();
        }
        int linked = definition.getLinkedNoteId();
        return linked >= 0 ? Optional.of(ItemId.of(linked)) : Optional.empty();
    }
}
