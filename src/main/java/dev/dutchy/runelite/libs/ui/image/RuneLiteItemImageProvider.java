package dev.dutchy.runelite.libs.ui.image;

import dev.dutchy.runelite.libs.ui.ItemUiException;
import net.runelite.client.game.ItemManager;
import net.runelite.client.util.AsyncBufferedImage;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Objects;

@Singleton
public final class RuneLiteItemImageProvider implements ItemImageProvider {

    private final ItemManager itemManager;

    @Inject
    public RuneLiteItemImageProvider(ItemManager itemManager) {
        this.itemManager = Objects.requireNonNull(itemManager, "itemManager");
    }

    @Override
    public ItemImage imageFor(ItemImageRequest request) {
        Objects.requireNonNull(request, "request");
        AsyncBufferedImage image = itemManager.getImage(request.itemId().value(), request.quantity(), request.showQuantity());
        if (image == null) {
            throw new ItemUiException("RuneLite could not create a sprite for " + request.itemId());
        }
        return new AsyncItemImage(image);
    }
}
