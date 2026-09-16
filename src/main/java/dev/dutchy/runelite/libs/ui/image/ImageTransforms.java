package dev.dutchy.runelite.libs.ui.image;

import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;

public final class ImageTransforms {

    private ImageTransforms() {
    }

    public static ImageTransform scaleDownToFit(int maxWidth, int maxHeight) {
        requirePositive(maxWidth, "maxWidth");
        requirePositive(maxHeight, "maxHeight");
        return source -> {
            if (source.getWidth() <= maxWidth && source.getHeight() <= maxHeight) {
                return source;
            }
            return scaleToFit(maxWidth, maxHeight).apply(source);
        };
    }

    public static ImageTransform scaleToFit(int maxWidth, int maxHeight) {
        requirePositive(maxWidth, "maxWidth");
        requirePositive(maxHeight, "maxHeight");
        return source -> {
            double scale = Math.min((double) maxWidth / source.getWidth(), (double) maxHeight / source.getHeight());
            int width = Math.max(1, (int) Math.round(source.getWidth() * scale));
            int height = Math.max(1, (int) Math.round(source.getHeight() * scale));
            if (width == source.getWidth() && height == source.getHeight()) {
                return source;
            }
            BufferedImage target = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = target.createGraphics();
            try {
                g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                g.drawImage(source, 0, 0, width, height, null);
            } finally {
                g.dispose();
            }
            return target;
        };
    }

    public static ImageTransform grayscale() {
        return source -> {
            BufferedImage argb = toArgb(source);
            BufferedImage gray = new BufferedImage(argb.getWidth(), argb.getHeight(), BufferedImage.TYPE_INT_ARGB);
            new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_GRAY), null).filter(argb, gray);

            for (int y = 0; y < argb.getHeight(); y++) {
                for (int x = 0; x < argb.getWidth(); x++) {
                    int alpha = argb.getRGB(x, y) & 0xFF000000;
                    gray.setRGB(x, y, alpha | (gray.getRGB(x, y) & 0x00FFFFFF));
                }
            }
            return gray;
        };
    }

    public static ImageTransform opacity(float opacity) {
        if (opacity < 0f || opacity > 1f) {
            throw new IllegalArgumentException("Opacity must be within [0, 1], got " + opacity);
        }
        return source -> {
            BufferedImage target = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = target.createGraphics();
            try {
                g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
                g.drawImage(source, 0, 0, null);
            } finally {
                g.dispose();
            }
            return target;
        };
    }

    private static BufferedImage toArgb(BufferedImage source) {
        if (source.getType() == BufferedImage.TYPE_INT_ARGB) {
            return source;
        }
        BufferedImage copy = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = copy.createGraphics();
        try {
            g.drawImage(source, 0, 0, null);
        } finally {
            g.dispose();
        }
        return copy;
    }

    private static void requirePositive(int value, String name) {
        if (value <= 0) {
            throw new IllegalArgumentException(name + " must be positive, got " + value);
        }
    }
}
