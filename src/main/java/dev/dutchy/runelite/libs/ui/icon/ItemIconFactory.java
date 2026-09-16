package dev.dutchy.runelite.libs.ui.icon;

import dev.dutchy.runelite.libs.ui.button.ItemLoader;
import dev.dutchy.runelite.libs.ui.image.ImageTransform;
import dev.dutchy.runelite.libs.ui.image.ItemImageOptions;
import dev.dutchy.runelite.libs.ui.swing.EdtDispatch;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.awt.*;
import java.util.Objects;

@Singleton
public final class ItemIconFactory {

    private final ItemLoader loader;

    @Inject
    public ItemIconFactory(ItemLoader loader) {
        this.loader = Objects.requireNonNull(loader, "loader");
    }

    public ItemLoader loader() {
        return loader;
    }

    public ItemIcon icon(int width, int height) {
        EdtDispatch.requireEdt();
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Icon size must be positive, got " + width + "x" + height);
        }
        return new ItemIcon(loader, new Dimension(width, height), ImageTransform.identity(), ItemImageOptions.DEFAULT);
    }
}
