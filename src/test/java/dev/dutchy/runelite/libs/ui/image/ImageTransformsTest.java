package dev.dutchy.runelite.libs.ui.image;

import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ImageTransformsTest {

    private static BufferedImage image(int width, int height) {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                img.setRGB(x, y, 0xFFFF0000);
            }
        }
        return img;
    }

    @Test
    void scaleDownToFitLeavesSmallImagesUntouched() {
        BufferedImage source = image(36, 32);
        assertSame(source, ImageTransforms.scaleDownToFit(40, 40).apply(source));
    }

    @Test
    void scaleDownToFitPreservesAspectRatio() {
        BufferedImage result = ImageTransforms.scaleDownToFit(18, 18).apply(image(36, 32));
        assertEquals(18, result.getWidth());
        assertEquals(16, result.getHeight());
    }

    @Test
    void scaleToFitUpscalesToBounds() {
        BufferedImage result = ImageTransforms.scaleToFit(72, 100).apply(image(36, 32));
        assertEquals(72, result.getWidth());
        assertEquals(64, result.getHeight());
    }

    @Test
    void grayscalePreservesAlpha() {
        BufferedImage source = image(2, 2);
        source.setRGB(0, 0, 0x80FF0000);
        BufferedImage result = ImageTransforms.grayscale().apply(source);
        assertEquals(0x80, (result.getRGB(0, 0) >>> 24));
        int pixel = result.getRGB(1, 1);
        int r = (pixel >> 16) & 0xFF;
        int g = (pixel >> 8) & 0xFF;
        int b = pixel & 0xFF;
        assertEquals(r, g);
        assertEquals(g, b);
    }

    @Test
    void opacityScalesAlpha() {
        BufferedImage result = ImageTransforms.opacity(0.5f).apply(image(1, 1));
        int alpha = result.getRGB(0, 0) >>> 24;
        assertEquals(128, alpha, 1);
    }

    @Test
    void transformsCompose() {
        ImageTransform composed = ImageTransforms.scaleToFit(18, 18).andThen(ImageTransforms.opacity(0.5f));
        BufferedImage result = composed.apply(image(36, 32));
        assertEquals(18, result.getWidth());
        assertEquals(128, result.getRGB(0, 0) >>> 24, 1);
    }

    @Test
    void validatesArguments() {
        assertThrows(IllegalArgumentException.class, () -> ImageTransforms.scaleToFit(0, 10));
        assertThrows(IllegalArgumentException.class, () -> ImageTransforms.opacity(1.5f));
    }
}
