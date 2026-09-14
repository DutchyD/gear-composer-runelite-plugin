package dev.dutchy.runelite.gear;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SetupNamesTest {

    @Test
    void copiesAreNumberedPastWhatIsTaken() {
        assertEquals("Vorkath (2)", SetupNames.copyOf("Vorkath", List.of("Vorkath")));
        assertEquals("Vorkath (3)", SetupNames.copyOf("Vorkath", List.of("Vorkath", "Vorkath (2)")));
        assertEquals("Vorkath (3)", SetupNames.copyOf("Vorkath (2)", List.of("Vorkath", "Vorkath (2)")), "a copy of a copy keeps one number");
    }

    @Test
    void longNamesAreShortenedToFitTheSuffix() {
        String name = SetupNames.copyOf("x".repeat(GearSetup.MAX_NAME_LENGTH), List.of());
        assertTrue(name.length() <= GearSetup.MAX_NAME_LENGTH);
        assertTrue(name.endsWith(" (2)"));
    }
}
