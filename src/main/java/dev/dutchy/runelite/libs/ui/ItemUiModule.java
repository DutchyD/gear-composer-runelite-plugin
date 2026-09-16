package dev.dutchy.runelite.libs.ui;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import dev.dutchy.runelite.libs.ui.button.DefaultItemLoader;
import dev.dutchy.runelite.libs.ui.button.ItemLoader;
import dev.dutchy.runelite.libs.ui.image.ItemImageProvider;
import dev.dutchy.runelite.libs.ui.image.RuneLiteItemImageProvider;
import dev.dutchy.runelite.libs.ui.item.*;
import dev.dutchy.runelite.libs.ui.search.*;

import javax.inject.Singleton;

/**
 * Guice bindings for the item UI library. Install from {@code Plugin#configure(Binder)}:
 * <pre>{@code binder.install(new ItemUiModule());}</pre>
 */
public final class ItemUiModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(ItemNameMatcher.class).to(BestNameMatch.class);
        bind(ItemImageProvider.class).to(RuneLiteItemImageProvider.class);
        bind(ItemLoader.class).to(DefaultItemLoader.class);
        bind(ItemCatalog.class).to(RuneLiteItemCatalog.class);
        bind(ItemIndexProvider.class).to(CachingItemIndex.class);
        bind(NameScorer.class).to(SubsequenceNameScorer.class);
        bind(ItemSearch.class).to(FuzzyItemSearch.class);
    }

    /** Ids come from the game, names from the full item catalogue. */
    @Provides
    @Singleton
    ItemResolver itemResolver(RuneLiteItemResolver byId, IndexItemResolver byName) {
        return ChainedItemResolver.of(byId, byName);
    }
}
