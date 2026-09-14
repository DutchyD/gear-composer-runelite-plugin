package dev.dutchy.runelite.gear.persistence;

import dev.dutchy.runelite.gear.GearSection;
import dev.dutchy.runelite.gear.GearSetup;
import dev.dutchy.runelite.gear.SectionId;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class BookMergerTest {

    @Test
    void importedSectionsAreAppendedAndClashingIdsRegenerated() {
        GearSetup shared = GearSetup.named("Vorkath");
        GearSection current = new GearSection(SectionId.random(), "Mine", List.of(shared));
        GearSection imported = new GearSection(current.id(), "Theirs", List.of(shared, GearSetup.named("Zulrah")));

        List<GearSection> merged = BookMerger.merge(List.of(current), List.of(imported));

        assertEquals(2, merged.size());
        assertEquals(current, merged.get(0));
        assertNotEquals(current.id(), merged.get(1).id());
        assertEquals("Theirs", merged.get(1).name());
        assertNotEquals(shared.id(), merged.get(1).setups().get(0).id());
        assertEquals("Vorkath", merged.get(1).setups().get(0).name());
        assertEquals("Zulrah", merged.get(1).setups().get(1).name());
    }
    @Test
    void aFamilyKeepsItsLinkWhenItsIdsAreRegenerated() {
        GearSection current = GearSection.named("Mine");
        GearSection raids = new GearSection(current.id(), "Raids", List.of());
        GearSection toa = new GearSection(SectionId.random(), "ToA", List.of(), raids.id());

        List<GearSection> merged = BookMerger.merge(List.of(current), List.of(raids, toa));

        assertEquals(3, merged.size());
        assertNotEquals(current.id(), merged.get(1).id());
        assertEquals(merged.get(1).id(), merged.get(2).parent().orElseThrow(), "the child follows the renamed parent");
    }

}
