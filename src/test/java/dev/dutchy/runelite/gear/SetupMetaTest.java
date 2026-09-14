package dev.dutchy.runelite.gear;

import java.awt.event.KeyEvent;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SetupMetaTest {

    @Test
    void tagsAreLowerCasedTrimmedSortedAndDeduplicated() {
        SetupMeta meta = SetupMeta.none().withTags(Set.of(" Melee", "bossing", "MELEE ", ""));
        assertEquals(List.of("bossing", "melee"), List.copyOf(meta.tags()));
    }

    @Test
    void tagLimitsAreEnforced() {
        assertThrows(IllegalArgumentException.class, () -> SetupMeta.none().withTags(Set.of("x".repeat(SetupMeta.MAX_TAG_LENGTH + 1))));
        Set<String> tooMany = new HashSet<>();
        for (int i = 0; i <= SetupMeta.MAX_TAGS; i++) {
            tooMany.add("tag" + i);
        }
        assertThrows(IllegalArgumentException.class, () -> SetupMeta.none().withTags(tooMany));
    }

    @Test
    void notesAreTrimmedAndCapped() {
        assertEquals("bring brews", SetupMeta.none().withNotes("  bring brews \n").notes());
        assertThrows(IllegalArgumentException.class, () -> SetupMeta.none().withNotes("x".repeat(SetupMeta.MAX_NOTES + 1)));
    }

    @Test
    void aCopyLosesItsPinAndHotkey() {
        GearSetup original = GearSetup.named("Vorkath").withMeta(meta -> meta
                .withPinned(true)
                .withHotkey(new Hotkey(KeyEvent.VK_F5, 0))
                .withLabel(ColourLabel.RED));
        GearSetup copy = original.copyNamed("Vorkath (2)");
        assertFalse(copy.meta().hasHotkey());
        assertFalse(copy.isPinned());
        assertEquals(ColourLabel.RED, copy.meta().label(), "labels are worth keeping on a copy");
        assertNotEquals(original.id(), copy.id());
    }

    @Test
    void requirementsKnowWhenTheyAreEmpty() {
        assertTrue(Requirements.none().isEmpty());
        assertFalse(Requirements.none().withSpellbook(Spellbook.LUNAR).isEmpty());
        assertEquals(Optional.of(Spellbook.ARCEUUS), Spellbook.fromGameValue(3));
        assertEquals(Optional.empty(), Spellbook.fromGameValue(9));
    }
}
