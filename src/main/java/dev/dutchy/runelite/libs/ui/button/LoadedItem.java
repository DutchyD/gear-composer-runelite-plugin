package dev.dutchy.runelite.libs.ui.button;

import dev.dutchy.runelite.libs.ui.image.ItemImage;
import dev.dutchy.runelite.libs.ui.item.ResolvedItem;
import lombok.Value;
import lombok.experimental.Accessors;

import java.util.Objects;

@Value
@Accessors(fluent = true)
public class LoadedItem {
    ResolvedItem item;
    ItemImage image;

    public LoadedItem(ResolvedItem item, ItemImage image) {
        Objects.requireNonNull(item, "item");
        Objects.requireNonNull(image, "image");
        this.item = item;
        this.image = image;
    }
}
