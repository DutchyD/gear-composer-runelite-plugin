package dev.dutchy.runelite.gear.share;

import java.awt.image.BufferedImage;
import java.io.IOException;

/** Where a rendered setup image can go. */
public interface ImageSink {

    /** Saves into the screenshots folder and returns a short description of where. */
    String save(BufferedImage image, String fileName) throws IOException;

    void copy(BufferedImage image);
}
