package dev.dutchy.runelite.libs.ui.icon;

import dev.dutchy.runelite.libs.ui.button.ItemLoader;
import dev.dutchy.runelite.libs.ui.button.LoadedItem;
import dev.dutchy.runelite.libs.ui.image.ImageTransform;
import dev.dutchy.runelite.libs.ui.image.ImageTransforms;
import dev.dutchy.runelite.libs.ui.image.ItemImageOptions;
import dev.dutchy.runelite.libs.ui.item.ItemReference;
import dev.dutchy.runelite.libs.ui.item.ResolvedItem;
import dev.dutchy.runelite.libs.ui.swing.EdtDispatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

/** All methods must be called on the EDT. */
public final class ItemIcon extends JComponent {

    private static final Logger log = LoggerFactory.getLogger(ItemIcon.class);

    private final ItemLoader loader;
    private final ItemImageOptions imageOptions;

    private ImageTransform transform;
    private Consumer<ResolvedItem> onResolved = resolved -> {
    };
    private ResolvedItem item;
    private BufferedImage rawSprite;
    private BufferedImage displaySprite;
    private int generation;

    ItemIcon(ItemLoader loader, Dimension size, ImageTransform transform, ItemImageOptions imageOptions) {
        this.loader = Objects.requireNonNull(loader, "loader");
        this.transform = Objects.requireNonNull(transform, "transform");
        this.imageOptions = Objects.requireNonNull(imageOptions, "imageOptions");
        Objects.requireNonNull(size, "size");
        setOpaque(false);
        setPreferredSize(new Dimension(size));
        setMinimumSize(new Dimension(size));
        setMaximumSize(new Dimension(size));
    }

    public void setItem(ItemReference reference) {
        EdtDispatch.requireEdt();
        Objects.requireNonNull(reference, "reference");
        int myGeneration = ++generation;
        item = null;
        rawSprite = null;
        displaySprite = null;
        repaint();
        loader.load(reference, imageOptions).whenComplete((result, error) -> EdtDispatch.onEdt(() -> {
            if (myGeneration != generation) {
                return;
            }
            if (error != null) {
                log.warn("Failed to load {}", reference.describe(), error);
            } else {
                result.ifPresent(loaded -> onLoaded(myGeneration, loaded));
            }
        }));
    }

    public void clearItem() {
        EdtDispatch.requireEdt();
        generation++;
        item = null;
        rawSprite = null;
        displaySprite = null;
        repaint();
    }

    /** Notified on the EDT when the item behind the sprite becomes known. */
    public void setResolvedListener(Consumer<ResolvedItem> listener) {
        EdtDispatch.requireEdt();
        onResolved = Objects.requireNonNull(listener, "listener");
    }

    public Optional<ResolvedItem> item() {
        return Optional.ofNullable(item);
    }

    public boolean hasSprite() {
        return displaySprite != null;
    }

    public void setImageTransform(ImageTransform newTransform) {
        EdtDispatch.requireEdt();
        transform = Objects.requireNonNull(newTransform, "newTransform");
        refreshDisplaySprite();
        repaint();
    }

    private void onLoaded(int myGeneration, LoadedItem loaded) {
        item = loaded.item();
        onResolved.accept(item);
        loaded.image().whenLoaded(sprite -> {
            if (myGeneration != generation) {
                return;
            }
            rawSprite = sprite;
            refreshDisplaySprite();
            repaint();
        });
    }

    private void refreshDisplaySprite() {
        if (rawSprite == null) {
            displaySprite = null;
            return;
        }
        Dimension size = getPreferredSize();
        displaySprite = transform.andThen(ImageTransforms.scaleDownToFit(size.width, size.height)).apply(rawSprite);
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (displaySprite == null) {
            return;
        }
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            int x = (getWidth() - displaySprite.getWidth()) / 2;
            int y = (getHeight() - displaySprite.getHeight()) / 2;
            g2.drawImage(displaySprite, x, y, null);
        } finally {
            g2.dispose();
        }
    }
}
