package dev.dutchy.runelite.libs.ui.button;

import dev.dutchy.runelite.libs.ui.image.RuneLiteItemImageProvider;
import dev.dutchy.runelite.libs.ui.item.ItemReference;
import dev.dutchy.runelite.libs.ui.item.ItemResolver;
import dev.dutchy.runelite.libs.ui.search.ItemResolvers;
import dev.dutchy.runelite.libs.ui.style.ItemButtonStyle;
import net.runelite.api.Client;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.game.ItemManager;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Objects;

@Singleton
public final class ItemButtonFactory {

    private final ItemLoader loader;
    private final ItemButtonStyle defaultStyle;

    @Inject
    public ItemButtonFactory(ItemLoader loader) {
        this(loader, ItemButtonStyle.runeLite());
    }

    public ItemButtonFactory(ItemLoader loader, ItemButtonStyle defaultStyle) {
        this.loader = Objects.requireNonNull(loader, "loader");
        this.defaultStyle = Objects.requireNonNull(defaultStyle, "defaultStyle");
    }

    public static ItemButtonFactory forRuneLite(Client client, ClientThread clientThread, ItemManager itemManager) {
        ItemResolver resolver = ItemResolvers.forRuneLite(client, clientThread);
        return new ItemButtonFactory(new DefaultItemLoader(resolver, new RuneLiteItemImageProvider(itemManager)));
    }

    public ItemButtonBuilder button(ItemReference reference) {
        return new ItemButtonBuilder(loader, defaultStyle, Objects.requireNonNull(reference, "reference"));
    }

    public ItemButtonBuilder button(int itemId) {
        return button(ItemReference.byId(itemId));
    }

    public ItemButtonBuilder button(String itemName) {
        return button(ItemReference.byName(itemName));
    }

    public ItemButtonBuilder emptyButton() {
        return new ItemButtonBuilder(loader, defaultStyle, null);
    }
}
