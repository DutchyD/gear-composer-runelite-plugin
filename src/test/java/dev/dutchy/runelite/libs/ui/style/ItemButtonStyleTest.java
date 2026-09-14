package dev.dutchy.runelite.libs.ui.style;

import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.awt.Dimension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ItemButtonStyleTest {

    @Test
    void contentSizeSubtractsPaddingAndBorder() {
        ItemButtonStyle style = ItemButtonStyle.builder().size(40, 36).padding(2).borderThickness(1).build();
        assertEquals(new Dimension(34, 30), style.contentSize());
    }

    @Test
    void contentSizeNeverDropsBelowOnePixel() {
        ItemButtonStyle style = ItemButtonStyle.builder().size(4, 4).padding(5).build();
        assertEquals(new Dimension(1, 1), style.contentSize());
    }

    @Test
    void toBuilderRoundTrips() {
        ItemButtonStyle original = ItemButtonStyle.compact().toBuilder().selectedBorder(Color.RED).build();
        assertEquals(Color.RED, original.selectedBorder());
        assertEquals(ItemButtonStyle.compact().size(), original.size());
    }

    @Test
    void accessorsReturnDefensiveCopies() {
        ItemButtonStyle style = ItemButtonStyle.runeLite();
        style.size().width = 1;
        assertEquals(40, style.size().width);
    }

    @Test
    void validatesArguments() {
        assertThrows(IllegalArgumentException.class, () -> ItemButtonStyle.builder().size(0, 10).build());
        assertThrows(IllegalArgumentException.class, () -> ItemButtonStyle.builder().borderThickness(-1).build());
        assertThrows(NullPointerException.class, () -> ItemButtonStyle.builder().background(null).build());
    }
}
