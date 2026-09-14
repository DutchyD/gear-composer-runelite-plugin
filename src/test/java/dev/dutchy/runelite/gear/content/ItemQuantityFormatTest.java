package dev.dutchy.runelite.gear.content;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ItemQuantityFormatTest {

    @Test
    void smallStacksShowExactly() {
        assertEquals("1", ItemQuantityFormat.text(1));
        assertEquals("99999", ItemQuantityFormat.text(99_999));
        assertEquals(ItemQuantityFormat.SMALL, ItemQuantityFormat.color(50_000));
    }

    @Test
    void thousandsAreAbbreviated() {
        assertEquals("100K", ItemQuantityFormat.text(100_000));
        assertEquals("9999K", ItemQuantityFormat.text(9_999_999));
        assertEquals(ItemQuantityFormat.THOUSANDS, ItemQuantityFormat.color(100_000));
    }

    @Test
    void millionsAreAbbreviated() {
        assertEquals("10M", ItemQuantityFormat.text(10_000_000));
        assertEquals(ItemQuantityFormat.MILLIONS, ItemQuantityFormat.color(10_000_000));
    }
    @Test
    void aMillionIsWrittenInThousandsUntilTenMillion() {
        assertEquals("1000K", ItemQuantityFormat.text(1_000_000));
        assertEquals("9999K", ItemQuantityFormat.text(9_999_999));
        assertEquals("10M", ItemQuantityFormat.text(10_000_000));
    }

}
