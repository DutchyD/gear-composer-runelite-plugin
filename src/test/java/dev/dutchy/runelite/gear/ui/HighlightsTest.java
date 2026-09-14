package dev.dutchy.runelite.gear.ui;

import net.runelite.client.ui.ColorScheme;
import org.junit.jupiter.api.Test;

import java.awt.Color;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HighlightsTest {

    @Test
    void theSelectionColourIsOpaqueSoRepaintsCannotStack() {
        assertEquals(255, Highlights.selection(ColorScheme.DARKER_GRAY_COLOR).getAlpha());
        assertEquals(255, Highlights.selection(ColorScheme.DARK_GRAY_COLOR).getAlpha());
    }

    @Test
    void theSelectionColourSitsBetweenTheSurfaceAndTheBrandOrange() {
        Color surface = ColorScheme.DARKER_GRAY_COLOR;
        Color blended = Highlights.selection(surface);
        assertTrue(blended.getRed() > surface.getRed());
        assertTrue(blended.getRed() < ColorScheme.BRAND_ORANGE.getRed());
        assertTrue(blended.getGreen() > surface.getGreen());
        assertTrue(blended.getGreen() < ColorScheme.BRAND_ORANGE.getGreen());
    }

    @Test
    void blendingIsStableForTheSameSurface() {
        assertEquals(Highlights.selection(ColorScheme.DARK_GRAY_COLOR),
                Highlights.selection(ColorScheme.DARK_GRAY_COLOR));
    }
}
