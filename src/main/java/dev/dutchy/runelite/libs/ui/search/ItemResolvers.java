package dev.dutchy.runelite.libs.ui.search;

import dev.dutchy.runelite.libs.ui.item.ChainedItemResolver;
import dev.dutchy.runelite.libs.ui.item.ItemResolver;
import dev.dutchy.runelite.libs.ui.item.RuneLiteItemResolver;
import net.runelite.api.Client;
import net.runelite.client.callback.ClientThread;

/** Composition helpers for plugins that do not use Guice bindings. */
public final class ItemResolvers {

    private ItemResolvers() {
    }

    /** Ids come from the game, names from the full item catalogue. */
    public static ItemResolver forRuneLite(Client client, ClientThread clientThread) {
        return ChainedItemResolver.of(
                new RuneLiteItemResolver(client, clientThread),
                new IndexItemResolver(ItemSearches.index(client, clientThread), ItemSearches.ranker()));
    }
}
