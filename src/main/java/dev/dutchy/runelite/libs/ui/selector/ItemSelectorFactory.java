package dev.dutchy.runelite.libs.ui.selector;

import dev.dutchy.runelite.libs.ui.button.ItemButtonFactory;
import dev.dutchy.runelite.libs.ui.item.ItemResolver;
import dev.dutchy.runelite.libs.ui.search.ItemResolvers;
import dev.dutchy.runelite.libs.ui.search.ItemSearch;
import dev.dutchy.runelite.libs.ui.search.ItemSearches;
import net.runelite.api.Client;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.game.ItemManager;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Objects;
import java.util.concurrent.Executor;

/**
 * Entry point for creating {@link ItemSelector}s. Inject it after installing
 * {@link dev.dutchy.runelite.libs.ui.ItemUiModule}, or compose one with {@link #forRuneLite}.
 */
@Singleton
public final class ItemSelectorFactory {

    private final ItemSearch search;
    private final ItemButtonFactory buttons;

    @Inject
    public ItemSelectorFactory(ItemSearch search, ItemButtonFactory buttons) {
        this.search = Objects.requireNonNull(search, "search");
        this.buttons = Objects.requireNonNull(buttons, "buttons");
    }

    /**
     * The library composed by hand, for a plugin that does not install {@link dev.dutchy.runelite.libs.ui.ItemUiModule}.
     *
     * @param executor where catalogue ranking runs; must not be the EDT or the client thread
     */
    @SuppressWarnings("unused")
    public static ItemSelectorFactory forRuneLite(Client client, ClientThread clientThread, ItemManager itemManager, Executor executor) {
        ItemResolver resolver = ItemResolvers.forRuneLite(client, clientThread);
        return new ItemSelectorFactory(
                ItemSearches.forRuneLite(client, clientThread, resolver, executor),
                ItemButtonFactory.forRuneLite(client, clientThread, itemManager));
    }

    public ItemSelectorBuilder selector() {
        return new ItemSelectorBuilder(search, buttons);
    }
}
