package dev.dutchy.runelite.libs.ui.image;

import java.awt.image.BufferedImage;
import java.util.function.Consumer;

public interface ItemImage {

    BufferedImage image();

    /** Runs the callback once, on the EDT. */
    void whenLoaded(Consumer<BufferedImage> callback);
}
