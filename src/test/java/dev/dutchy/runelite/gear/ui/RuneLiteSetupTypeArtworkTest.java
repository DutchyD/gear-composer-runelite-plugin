package dev.dutchy.runelite.gear.ui;

import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RuneLiteSetupTypeArtworkTest {

    private static BufferedImage square(int size, Color color) {
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setColor(color);
        g.fillRect(0, 0, size, size);
        g.dispose();
        return image;
    }

    @Test
    void theCustomArtworkStacksTheSecondIconOverTheFirstTowardsTheBottomRight() {
        BufferedImage stacked = RuneLiteSetupTypeArtwork.stacked(square(50, Color.RED), square(20, Color.BLUE));

        assertEquals(RuneLiteSetupTypeArtwork.STACK_SIZE, stacked.getWidth());
        assertEquals(Color.RED.getRGB(), stacked.getRGB(2, 2), "the back icon fills the top left");
        assertEquals(Color.BLUE.getRGB(), stacked.getRGB(RuneLiteSetupTypeArtwork.STACK_SIZE - 3, RuneLiteSetupTypeArtwork.STACK_SIZE - 3), "the front icon reaches the bottom right");
        assertEquals(Color.BLUE.getRGB(), stacked.getRGB(RuneLiteSetupTypeArtwork.STACK_ICON - 2, RuneLiteSetupTypeArtwork.STACK_ICON - 2), "the front icon covers where they overlap");
        assertEquals(0, stacked.getRGB(RuneLiteSetupTypeArtwork.STACK_SIZE - 2, 1) >>> 24, "the corners off both icons stay clear");
    }
}
