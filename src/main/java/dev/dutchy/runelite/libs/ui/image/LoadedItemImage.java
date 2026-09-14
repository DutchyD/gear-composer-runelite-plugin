package dev.dutchy.runelite.libs.ui.image;

import javax.swing.*;
import java.awt.image.BufferedImage;
import java.util.Objects;
import java.util.function.Consumer;

public final class LoadedItemImage implements ItemImage {

    private final BufferedImage image;

    public LoadedItemImage(BufferedImage image) {
        this.image = Objects.requireNonNull(image, "image");
    }

    @Override
    public BufferedImage image() {
        return image;
    }

    @Override
    public void whenLoaded(Consumer<BufferedImage> callback) {
        Objects.requireNonNull(callback, "callback");
        SwingUtilities.invokeLater(() -> callback.accept(image));
    }

    @Override
    public String toString() {
        return "LoadedItemImage[" + image.getWidth() + "x" + image.getHeight() + "]";
    }
}
