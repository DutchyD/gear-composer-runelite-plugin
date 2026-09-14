package dev.dutchy.runelite.libs.ui.button;

import lombok.Value;
import lombok.experimental.Accessors;
import dev.dutchy.runelite.libs.ui.image.ItemImageOptions;
import dev.dutchy.runelite.libs.ui.image.LoadedItemImage;
import dev.dutchy.runelite.libs.ui.item.ItemReference;
import dev.dutchy.runelite.libs.ui.item.ResolvedItem;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

final class FakeItemLoader implements ItemLoader {

    @Value
    @Accessors(fluent = true)
    static class Request {
        ItemReference reference;
        ItemImageOptions options;
        CompletableFuture<Optional<LoadedItem>> future;


        void completeWith(ResolvedItem item) {
            future.complete(Optional.of(new LoadedItem(item, new LoadedItemImage(sprite()))));
        }

        void completeEmpty() {
            future.complete(Optional.empty());
        }

        void fail(Throwable error) {
            future.completeExceptionally(error);
        }
    }

    final List<Request> requests = new ArrayList<>();

    @Override
    public CompletableFuture<Optional<LoadedItem>> load(ItemReference reference, ItemImageOptions options) {
        Request request = new Request(reference, options, new CompletableFuture<>());
        requests.add(request);
        return request.future;
    }

    Request last() {
        return requests.get(requests.size() - 1);
    }

    static BufferedImage sprite() {
        return new BufferedImage(36, 32, BufferedImage.TYPE_INT_ARGB);
    }
}
