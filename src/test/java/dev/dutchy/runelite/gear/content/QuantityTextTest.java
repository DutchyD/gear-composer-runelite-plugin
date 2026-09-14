package dev.dutchy.runelite.gear.content;

import org.junit.jupiter.api.Test;

import java.util.OptionalInt;

import static org.junit.jupiter.api.Assertions.assertEquals;

class QuantityTextTest {

    @Test
    void plainNumbersParse() {
        assertEquals(OptionalInt.of(1), QuantityText.parse("1"));
        assertEquals(OptionalInt.of(250), QuantityText.parse(" 250 "));
    }

    @Test
    void suffixesMultiply() {
        assertEquals(OptionalInt.of(100_000), QuantityText.parse("100K"));
        assertEquals(OptionalInt.of(10_000_000), QuantityText.parse("10m"));
        assertEquals(OptionalInt.of(1_000_000_000), QuantityText.parse("1B"));
        assertEquals(OptionalInt.of(2_500_000), QuantityText.parse("2.5M"));
    }

    @Test
    void maxIsTheLargestStackTheGameAllows() {
        assertEquals(OptionalInt.of(Integer.MAX_VALUE), QuantityText.parse("MAX"));
        assertEquals(OptionalInt.of(Integer.MAX_VALUE), QuantityText.parse("max"));
    }

    @Test
    void anythingElseIsRejected() {
        assertEquals(OptionalInt.empty(), QuantityText.parse(""));
        assertEquals(OptionalInt.empty(), QuantityText.parse("nonsense"));
        assertEquals(OptionalInt.empty(), QuantityText.parse("0"));
        assertEquals(OptionalInt.empty(), QuantityText.parse("-5"));
        assertEquals(OptionalInt.empty(), QuantityText.parse("1.5"));
        assertEquals(OptionalInt.empty(), QuantityText.parse("3B"), "past the largest stack");
        assertEquals(OptionalInt.empty(), QuantityText.parse("K"));
    }
}
