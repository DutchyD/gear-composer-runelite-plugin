package dev.dutchy.runelite.gear.ui;

import org.junit.jupiter.api.Test;

import javax.swing.JLabel;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ActionIconTest {

    private static BufferedImage paint(ActionIcon icon, Color foreground) {
        BufferedImage image = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        JLabel host = new JLabel();
        host.setForeground(foreground);
        try {
            icon.paintIcon(host, g, 0, 0);
        } finally {
            g.dispose();
        }
        return image;
    }

    private static long paintedPixels(BufferedImage image) {
        long count = 0;
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                if ((image.getRGB(x, y) >>> 24) != 0) {
                    count++;
                }
            }
        }
        return count;
    }

    @Test
    void everyIconDrawsSomething() {
        for (ActionIcon icon : ActionIcon.values()) {
            assertTrue(paintedPixels(paint(icon, Color.WHITE)) > 5, icon + " drew nothing");
        }
    }

    @Test
    void iconsAreSquareAndFitTheSectionHeader() {
        for (ActionIcon icon : ActionIcon.values()) {
            assertEquals(icon.getIconWidth(), icon.getIconHeight());
            assertTrue(icon.getIconWidth() <= 12, icon + " is too large for the header");
        }
    }

    @Test
    void iconsTakeTheHostForegroundSoHoverRecolouringWorks() {
        for (ActionIcon icon : ActionIcon.values()) {
            BufferedImage image = paint(icon, Color.RED);
            boolean foundRed = false;
            for (int y = 0; y < image.getHeight() && !foundRed; y++) {
                for (int x = 0; x < image.getWidth(); x++) {
                    int pixel = image.getRGB(x, y);
                    if ((pixel >>> 24) > 200 && (pixel & 0x00FFFFFF) == 0xFF0000) {
                        foundRed = true;
                        break;
                    }
                }
            }
            assertTrue(foundRed, icon + " ignored the host foreground");
        }
    }
}
