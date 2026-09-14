package dev.dutchy.runelite.gear;

import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PrayerTest {

    @Test
    void quickPrayerBitsRoundTripInBookOrder() {
        Set<Prayer> chosen = EnumSet.of(Prayer.PIETY, Prayer.PROTECT_FROM_MELEE, Prayer.THICK_SKIN);
        int bits = Prayer.toQuickPrayerBits(chosen);
        assertEquals((1 << 25) | (1 << 14) | 1, bits);
        assertEquals(chosen, Prayer.fromQuickPrayerBits(bits));
        assertTrue(Prayer.fromQuickPrayerBits(0).isEmpty());
        assertEquals("Thick Skin, Protect from Melee, Piety", Prayer.describe(chosen));
    }

    @Test
    void requirementsHoldPrayersBesideTheSpellbookAndNotes() {
        Requirements requirements = new Requirements(Spellbook.LUNAR, Set.of(Prayer.AUGURY, Prayer.RIGOUR));
        assertTrue(requirements.hasQuickPrayers());
        assertEquals(EnumSet.of(Prayer.RIGOUR, Prayer.AUGURY), requirements.quickPrayers());
        assertTrue(Requirements.none().isEmpty());
        assertFalse(new Requirements(Spellbook.ANY, Set.of(Prayer.PIETY)).isEmpty(), "prayers alone make a requirement");
        assertEquals(Set.of(), requirements.withQuickPrayers(Set.of()).quickPrayers());
    }
}
