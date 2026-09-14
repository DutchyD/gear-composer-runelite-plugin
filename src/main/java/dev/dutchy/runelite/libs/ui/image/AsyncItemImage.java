package dev.dutchy.runelite.libs.ui.image;

import net.runelite.client.util.AsyncBufferedImage;

import javax.swing.*;
import java.awt.image.BufferedImage;
import java.util.Objects;
import java.util.function.Consumer;

public final class AsyncItemImage implements ItemImage {

    private final AsyncBufferedImage image;

    public AsyncItemImage(AsyncBufferedImage image) {
        this.image = Objects.requireNonNull(image, "image");
    }

    @Override
    public BufferedImage image() {
        return image;
    }

    @Override
    public void whenLoaded(Consumer<BufferedImage> callback) {
        Objects.requireNonNull(callback, "callback");
        image.onLoaded(() -> SwingUtilities.invokeLater(() -> callback.accept(image)));
    }
}
