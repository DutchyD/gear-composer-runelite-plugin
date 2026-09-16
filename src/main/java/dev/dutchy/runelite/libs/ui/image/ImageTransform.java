package dev.dutchy.runelite.libs.ui.image;

import java.awt.image.BufferedImage;
import java.util.Objects;

/** Implementations must not mutate the source image. */
@FunctionalInterface
public interface ImageTransform {

    BufferedImage apply(BufferedImage source);

    static ImageTransform identity() {
        return source -> source;
    }

    default ImageTransform andThen(ImageTransform next) {
        Objects.requireNonNull(next, "next");
        return source -> next.apply(apply(source));
    }
}
