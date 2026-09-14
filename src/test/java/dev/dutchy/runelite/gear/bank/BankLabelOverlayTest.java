package dev.dutchy.runelite.gear.bank;

import dev.dutchy.runelite.gear.content.TextAlign;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BankLabelOverlayTest {

    @Test
    void textSitsAtTheLeftCentreOrRightOfTheColumnsItSpans() {
        assertEquals(100, BankLabelOverlay.alignedX(TextAlign.LEFT, 100, 200, 40));
        assertEquals(130, BankLabelOverlay.alignedX(TextAlign.CENTRE, 100, 200, 40));
        assertEquals(160, BankLabelOverlay.alignedX(TextAlign.RIGHT, 100, 200, 40));
    }
}
