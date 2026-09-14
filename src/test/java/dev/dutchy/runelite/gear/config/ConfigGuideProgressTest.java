package dev.dutchy.runelite.gear.config;

import dev.dutchy.runelite.gear.guide.GuideId;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConfigGuideProgressTest {

    @Test
    void readsATolerantListAndWritesItInAStableOrder() {
        assertEquals(Set.of(GuideId.LIST, GuideId.EDITOR), ConfigGuideProgress.parse("EDITOR, NO_SUCH_GUIDE,LIST"));
        assertTrue(ConfigGuideProgress.parse(null).isEmpty());
        assertTrue(ConfigGuideProgress.parse("  ").isEmpty());
        assertEquals("LIST,EDITOR,SLOT", ConfigGuideProgress.serialise(EnumSet.of(GuideId.SLOT, GuideId.LIST, GuideId.EDITOR)));
    }
}
