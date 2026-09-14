package dev.dutchy.runelite.gear;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SectionTreeTest {

    private static final GearSection RAIDS = GearSection.named("Raids");
    private static final GearSection TOA = new GearSection(SectionId.random(), "ToA", List.of(), RAIDS.id());
    private static final GearSection COX = new GearSection(SectionId.random(), "CoX", List.of(), RAIDS.id());
    private static final GearSection SLAYER = GearSection.named("Slayer");

    private static List<String> names(List<GearSection> sections) {
        return sections.stream().map(GearSection::name).collect(Collectors.toList());
    }

    @Test
    void childrenAreGatheredStraightAfterTheirParent() {
        List<GearSection> ordered = SectionTree.normalised(List.of(TOA, RAIDS, SLAYER, COX));
        assertEquals(List.of("Raids", "ToA", "CoX", "Slayer"), names(ordered));
    }

    @Test
    void orphansAndGrandchildrenAreLiftedToTheTopLevel() {
        GearSection orphan = new GearSection(SectionId.random(), "Orphan", List.of(), SectionId.random());
        GearSection grandchild = new GearSection(SectionId.random(), "Deep", List.of(), TOA.id());
        List<GearSection> ordered = SectionTree.normalised(List.of(RAIDS, TOA, grandchild, orphan));
        assertEquals(List.of("Raids", "ToA", "Deep", "Orphan"), names(ordered));
        assertFalse(ordered.get(2).isChild());
        assertFalse(ordered.get(3).isChild());
    }

    @Test
    void familiesAndPeersFollowTheOneLevelRule() {
        List<GearSection> sections = List.of(RAIDS, TOA, COX, SLAYER);
        assertEquals(List.of("Raids", "ToA", "CoX"), names(SectionTree.familyOf(sections, RAIDS.id())));
        assertEquals(List.of("ToA"), names(SectionTree.familyOf(sections, TOA.id())));
        assertEquals(List.of("ToA", "CoX"), names(SectionTree.peersOf(sections, COX.id())));
        assertEquals(List.of("Raids", "Slayer"), names(SectionTree.peersOf(sections, SLAYER.id())));
        assertTrue(SectionTree.hasChildren(sections, RAIDS.id()));
        assertFalse(SectionTree.hasChildren(sections, SLAYER.id()));
    }

    @Test
    void aSectionCannotBeItsOwnParent() {
        SectionId id = SectionId.random();
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class,
                () -> new GearSection(id, "Loop", List.of(), id));
    }
}
