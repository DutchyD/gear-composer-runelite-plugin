package dev.dutchy.runelite.libs.ui.button;

import dev.dutchy.runelite.libs.ui.image.ItemImageOptions;
import dev.dutchy.runelite.libs.ui.image.ItemImageProvider;
import dev.dutchy.runelite.libs.ui.item.ItemReference;
import dev.dutchy.runelite.libs.ui.item.ItemResolver;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Singleton
public final class DefaultItemLoader implements ItemLoader {

    private final ItemResolver resolver;
    private final ItemImageProvider imageProvider;

    @Inject
    public DefaultItemLoader(ItemResolver resolver, ItemImageProvider imageProvider) {
        this.resolver = Objects.requireNonNull(resolver, "resolver");
        this.imageProvider = Objects.requireNonNull(imageProvider, "imageProvider");
    }

    @Override
    public CompletableFuture<Optional<LoadedItem>> load(ItemReference reference, ItemImageOptions options) {
        Objects.requireNonNull(reference, "reference");
        Objects.requireNonNull(options, "options");
        return resolver.resolve(reference)
                .thenApply(resolved -> resolved.map(item ->
                        new LoadedItem(item, imageProvider.imageFor(options.toRequest(item.id())))));
    }
}
