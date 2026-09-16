package dev.dutchy.runelite.libs.ui.search;

import dev.dutchy.runelite.libs.ui.item.ItemResolver;
import net.runelite.api.Client;
import net.runelite.client.callback.ClientThread;

import java.util.concurrent.Executor;

/** Composition helpers for plugins that do not use Guice bindings. */
public final class ItemSearches {

    private ItemSearches() {
    }

    public static ItemIndexProvider index(Client client, ClientThread clientThread) {
        return new CachingItemIndex(new RuneLiteItemCatalog(client, clientThread));
    }

    public static ItemSearch forRuneLite(Client client, ClientThread clientThread, ItemResolver resolver, Executor executor) {
        return new FuzzyItemSearch(index(client, clientThread), ranker(), resolver, executor);
    }

    public static ItemRanker ranker() {
        return new ItemRanker(new SubsequenceNameScorer());
    }
}
