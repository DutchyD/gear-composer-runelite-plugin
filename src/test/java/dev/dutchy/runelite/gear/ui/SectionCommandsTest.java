package dev.dutchy.runelite.gear.ui;

import dev.dutchy.runelite.gear.GearSection;
import dev.dutchy.runelite.gear.GearSetup;
import dev.dutchy.runelite.gear.GearSetupBook;
import dev.dutchy.runelite.gear.SectionId;
import dev.dutchy.runelite.gear.SetupOrder;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SectionCommandsTest {

    private final GearSetupBook book = new GearSetupBook();
    private final RecordingPrompts prompts = new RecordingPrompts();
    private final SectionCommands sections = new SectionCommands(book);
    private final List<SectionId> renaming = new ArrayList<>();
    private int folds;

    private final SectionId first = book.sections().get(0).id();

    SectionCommandsTest() {
        sections.shownBy(prompts, new SectionCommands.Listener() {
            @Override
            public void renameStarted(SectionId sectionId) {
                renaming.add(sectionId);
            }

            @Override
            public void foldingChanged() {
                folds++;
            }
        }, this::drawn);
    }

    /** Every section in book order, which is what the list draws when nothing is folded. */
    private List<SectionId> drawn() {
        return book.sections().stream().map(GearSection::id).collect(Collectors.toList());
    }

    private List<String> names() {
        return book.sections().stream().map(GearSection::name).collect(Collectors.toList());
    }

    @Test
    void aNewSectionIsAddedWithItsNameWaitingToBeTyped() {
        sections.create();

        assertEquals(2, book.sectionCount());
        assertEquals(SectionCommands.NEW_SECTION_NAME, names().get(1));
        assertEquals(1, renaming.size(), "the list opens the field on it");
        assertEquals("Added a section", prompts.lastMessage());
    }

    @Test
    void aSectionCanBeInsertedOnEitherSideOfAnother() {
        SectionId second = book.addSection("Slayer").id();

        sections.insertAbove(second);
        sections.insertBelow(second);

        assertEquals(List.of(SectionCommands.NEW_SECTION_NAME, "Slayer", SectionCommands.NEW_SECTION_NAME), names().subList(1, 4));
        assertEquals(2, renaming.size(), "both new sections want their names typed");
    }

    @Test
    void renamingTakesTheNameOnlyWhenItIsAllowed() {
        sections.rename(first, "Bossing");
        assertEquals("Bossing", names().get(0));
        assertEquals("Renamed to Bossing", prompts.lastMessage());

        sections.rename(first, "   ");
        assertEquals("Bossing", names().get(0), "a blank name is refused");
    }

    @Test
    void deletingSaysWhatGoesWithItAndOnlyThenRemovesIt() {
        SectionId raids = book.addSection("Raids").id();
        book.addSetup(raids, "Vorkath");
        book.addSubSection(raids, "ToA");
        prompts.answering(false);

        sections.delete(raids);

        assertTrue(names().contains("Raids"), "answering no keeps it");
        String asked = prompts.questions().get(0);
        assertTrue(asked.contains("its 1 setup(s)"), asked);
        assertTrue(asked.contains("1 sub-section(s) move to the top level"), asked);

        prompts.answering(true);
        sections.delete(raids);

        assertFalse(names().contains("Raids"));
        assertTrue(names().contains("ToA"), "its children moved up a level");
    }

    @Test
    void anEmptySectionIsDeletedWithASimplerQuestion() {
        SectionId slayer = book.addSection("Slayer").id();

        sections.delete(slayer);

        assertEquals(List.of("Delete \"Slayer\"?"), prompts.questions());
        assertFalse(names().contains("Slayer"));
    }

    @Test
    void deletingOneThatIsAlreadyGoneAsksNothing() {
        SectionId slayer = book.addSection("Slayer").id();
        book.removeSection(slayer);

        sections.delete(slayer);

        assertTrue(prompts.questions().isEmpty());
    }

    @Test
    void sortingReportsTheOrderItWasPutIn() {
        book.addSetup(first, "Zulrah");
        book.addSetup(first, "Vorkath");

        sections.sort(first, SetupOrder.NAME);

        assertEquals(List.of("Vorkath", "Zulrah"),
                book.sections().get(0).setups().stream().map(GearSetup::name).collect(Collectors.toList()));
        assertTrue(prompts.lastMessage().startsWith("Sorted by"), prompts.lastMessage());
    }

    @Test
    void nestingAndUnnestingSayWhereTheSectionWent() {
        SectionId raids = book.addSection("Raids").id();
        SectionId toa = book.addSection("ToA").id();
        sections.toggleFolded(raids);

        sections.nest(toa, raids);

        assertTrue(book.section(toa).orElseThrow().isChild());
        assertEquals("Moved ToA under Raids", prompts.lastMessage());
        assertFalse(sections.isFolded(raids), "the new parent opens so the child can be seen");

        sections.unnest(toa);

        assertFalse(book.section(toa).orElseThrow().isChild());
        assertEquals("Moved ToA to the top level", prompts.lastMessage());
    }

    @Test
    void aDropCountsOnlyThePeersAboveTheCaret() {
        SectionId raids = book.addSection("Raids").id();
        SectionId toa = book.addSubSection(raids, "ToA").id();
        book.addSubSection(raids, "CoX");
        SectionId slayer = book.addSection("Slayer").id();

        sections.dropped(slayer, 1);

        assertEquals(List.of("Slayer", "Raids"), names().subList(1, 3), "a top-level section lands among top-level peers");

        sections.dropped(toa, 5);

        assertEquals(List.of("CoX", "ToA"), book.sections().stream().filter(GearSection::isChild)
                .map(GearSection::name).collect(Collectors.toList()), "a child lands among its siblings");
    }

    @Test
    void foldingOneSectionLeavesTheRestAloneUntilOthersAreFolded() {
        SectionId raids = book.addSection("Raids").id();
        SectionId slayer = book.addSection("Slayer").id();

        sections.toggleFolded(raids);

        assertTrue(sections.isFolded(raids));
        assertFalse(sections.isFolded(slayer));
        assertEquals(1, folds);

        sections.foldOthers(slayer);

        assertTrue(sections.isFolded(raids));
        assertTrue(sections.isFolded(first));
        assertFalse(sections.isFolded(slayer), "the one asked for stays open");
    }

    @Test
    void foldingASubSectionLeavesItsParentOpen() {
        SectionId raids = book.addSection("Raids").id();
        SectionId toa = book.addSubSection(raids, "ToA").id();

        sections.foldOthers(toa);

        assertFalse(sections.isFolded(raids), "a child cannot be seen through a folded parent");
        assertTrue(sections.isFolded(first));
    }

    @Test
    void aFoldIsForgottenWhenItsSectionIsGone() {
        SectionId slayer = book.addSection("Slayer").id();
        sections.toggleFolded(slayer);
        book.removeSection(slayer);

        sections.dropMissingFolds();

        assertFalse(sections.isFolded(slayer));
    }
}
