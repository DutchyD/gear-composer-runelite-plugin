package dev.dutchy.runelite.gear.share;

import dev.dutchy.runelite.gear.GearSetup;
import dev.dutchy.runelite.gear.content.SetupContentEditor;
import dev.dutchy.runelite.gear.content.SetupItem;
import dev.dutchy.runelite.libs.ui.button.ItemLoader;
import dev.dutchy.runelite.libs.ui.button.LoadedItem;
import dev.dutchy.runelite.libs.ui.image.ItemImageOptions;
import dev.dutchy.runelite.libs.ui.item.ItemId;
import dev.dutchy.runelite.libs.ui.item.ItemReference;
import dev.dutchy.runelite.libs.ui.swing.EdtDispatch;

import java.awt.image.BufferedImage;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

/** Every sprite and name a setup needs, gathered before anything is rendered. Completes on the EDT. */
public final class LoadedSetup {

    private final Map<ItemId, String> names;
    private final Map<ItemId, BufferedImage> sprites;

    private LoadedSetup(Map<ItemId, String> names, Map<ItemId, BufferedImage> sprites) {
        this.names = Map.copyOf(names);
        this.sprites = Map.copyOf(sprites);
    }

    public static CompletableFuture<LoadedSetup> load(GearSetup setup, ItemLoader loader) {
        Objects.requireNonNull(setup, "setup");
        Objects.requireNonNull(loader, "loader");
        List<ItemId> ids = new ArrayList<>();
        for (SetupItem item : SetupContentEditor.allItems(setup.content())) {
            if (!ids.contains(item.id())) {
                ids.add(item.id());
            }
        }
        Map<ItemId, String> names = new HashMap<>();
        Map<ItemId, BufferedImage> sprites = new HashMap<>();
        CompletableFuture<LoadedSetup> result = new CompletableFuture<>();
        if (ids.isEmpty()) {
            result.complete(new LoadedSetup(names, sprites));
            return result;
        }
        AtomicInteger pending = new AtomicInteger(ids.size());
        for (ItemId id : ids) {
            loader.load(ItemReference.byId(id), ItemImageOptions.DEFAULT).whenComplete((loaded, error) -> EdtDispatch.onEdt(() -> {
                if (error == null && loaded != null && loaded.isPresent()) {
                    LoadedItem item = loaded.get();
                    names.put(id, item.item().name());
                    item.image().whenLoaded(image -> {
                        sprites.put(id, image);
                        if (pending.decrementAndGet() == 0) {
                            result.complete(new LoadedSetup(names, sprites));
                        }
                    });
                } else if (pending.decrementAndGet() == 0) {
                    result.complete(new LoadedSetup(names, sprites));
                }
            }));
        }
        return result;
    }

    public Optional<String> name(ItemId id) {
        return Optional.ofNullable(names.get(id));
    }

    public Optional<BufferedImage> sprite(ItemId id) {
        return Optional.ofNullable(sprites.get(id));
    }
}
