package dev.dutchy.runelite.gear;

import dev.dutchy.runelite.gear.content.BankContent;
import dev.dutchy.runelite.libs.ui.item.ItemId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GearSetupBookTest {

    private GearSetupBook book;
    private SectionId first;

    @BeforeEach
    void setUp() {
        book = new GearSetupBook();
        first = book.sections().get(0).id();
    }

    private List<String> sectionNames() {
        return book.sections().stream().map(GearSection::name).collect(Collectors.toList());
    }

    private List<String> namesIn(SectionId sectionId) {
        return book.section(sectionId).orElseThrow().setups().stream().map(GearSetup::name).collect(Collectors.toList());
    }

    private SetupId add(SectionId section, String name) {
        return book.addSetup(section, name).id();
    }

    @Test
    void startsWithOneEmptySection() {
        assertEquals(1, book.sectionCount());
        assertEquals(GearSetupBook.DEFAULT_SECTION_NAME, book.sections().get(0).name());
        assertTrue(book.isEmpty());
    }

    @Test
    void addsSetupsInOrder() {
        add(first, "Vorkath");
        add(first, "Zulrah");
        assertEquals(List.of("Vorkath", "Zulrah"), namesIn(first));
        assertEquals(2, book.setupCount());
        assertFalse(book.isEmpty());
    }

    @Test
    void trimsAndValidatesNames() {
        assertEquals("Vorkath", book.addSetup(first, "  Vorkath  ").name());
        assertThrows(IllegalArgumentException.class, () -> book.addSetup(first, "   "));
        assertThrows(IllegalArgumentException.class, () -> book.addSetup(first, "x".repeat(200)));
    }

    @Test
    void updatesNameAndIcon() {
        SetupId id = add(first, "Vorkath");
        book.change(id, setup -> setup.withName("Vorkath melee").withIcon(ItemId.of(4151)));
        GearSetup updated = book.setup(id).orElseThrow();
        assertEquals("Vorkath melee", updated.name());
        assertEquals(Optional.of(ItemId.of(4151)), updated.icon());
        assertTrue(updated.hasIcon());
    }

    @Test
    void removesSetups() {
        SetupId id = add(first, "Vorkath");
        add(first, "Zulrah");
        book.removeSetup(id);
        assertEquals(List.of("Zulrah"), namesIn(first));
        assertTrue(book.setup(id).isEmpty());
    }

    @Test
    void movesSetupWithinASection() {
        SetupId vorkath = add(first, "Vorkath");
        add(first, "Zulrah");
        add(first, "Jad");

        assertTrue(book.moveSetup(vorkath, first, 2));
        assertEquals(List.of("Zulrah", "Jad", "Vorkath"), namesIn(first));
    }

    @Test
    void movingToTheSamePlaceChangesNothing() {
        SetupId vorkath = add(first, "Vorkath");
        add(first, "Zulrah");
        assertFalse(book.moveSetup(vorkath, first, 0));
        assertEquals(List.of("Vorkath", "Zulrah"), namesIn(first));
    }

    @Test
    void movesSetupBetweenSections() {
        SectionId bosses = book.addSection("Bosses").id();
        SetupId vorkath = add(first, "Vorkath");
        add(bosses, "Zulrah");

        book.moveSetup(vorkath, bosses, 0);

        assertEquals(List.of(), namesIn(first));
        assertEquals(List.of("Vorkath", "Zulrah"), namesIn(bosses));
        assertEquals(bosses, book.sectionOf(vorkath).orElseThrow().id());
    }

    @Test
    void clampsOutOfRangeDropIndexes() {
        SetupId vorkath = add(first, "Vorkath");
        add(first, "Zulrah");

        book.moveSetup(vorkath, first, 99);
        assertEquals(List.of("Zulrah", "Vorkath"), namesIn(first));

        book.moveSetup(vorkath, first, -5);
        assertEquals(List.of("Vorkath", "Zulrah"), namesIn(first));
    }

    @Test
    void movesSectionsUpAndDown() {
        SectionId bosses = book.addSection("Bosses").id();
        SectionId skilling = book.addSection("Skilling").id();

        assertTrue(book.moveSectionDown(first));
        assertEquals(List.of("Bosses", GearSetupBook.DEFAULT_SECTION_NAME, "Skilling"), sectionNames());

        assertTrue(book.moveSectionUp(skilling));
        assertEquals(List.of("Bosses", "Skilling", GearSetupBook.DEFAULT_SECTION_NAME), sectionNames());
        assertEquals(0, book.sectionIndex(bosses));
    }

    @Test
    void movesASectionToAnExplicitIndex() {
        book.addSection("Bosses");
        SectionId skilling = book.addSection("Skilling").id();

        assertTrue(book.moveSection(skilling, 0));
        assertEquals(List.of("Skilling", GearSetupBook.DEFAULT_SECTION_NAME, "Bosses"), sectionNames());
    }

    @Test
    void movingASectionNowhereChangesNothing() {
        book.addSection("Bosses");
        assertFalse(book.moveSection(first, 0));
        assertEquals(List.of(GearSetupBook.DEFAULT_SECTION_NAME, "Bosses"), sectionNames());
    }

    @Test
    void clampsOutOfRangeSectionIndexes() {
        SectionId bosses = book.addSection("Bosses").id();

        book.moveSection(bosses, 99);
        assertEquals(List.of(GearSetupBook.DEFAULT_SECTION_NAME, "Bosses"), sectionNames());

        book.moveSection(bosses, -5);
        assertEquals(List.of("Bosses", GearSetupBook.DEFAULT_SECTION_NAME), sectionNames());
    }

    @Test
    void movingASectionKeepsItsSetups() {
        SectionId bosses = book.addSection("Bosses").id();
        add(bosses, "Vorkath");
        book.moveSection(bosses, 0);
        assertEquals(List.of("Vorkath"), namesIn(bosses));
    }

    @Test
    void movingASectionNotifiesListeners() {
        book.addSection("Bosses");
        AtomicInteger changes = new AtomicInteger();
        book.addChangeListener(changed -> changes.incrementAndGet());
        book.moveSectionDown(first);
        assertEquals(1, changes.get());
    }

    @Test
    void deleteAllRemovesSetupsAndSectionsInOneChange() {
        SectionId bosses = book.addSection("Bosses").id();
        SetupId vorkath = add(first, "Vorkath");
        add(first, "Zulrah");
        add(bosses, "Jad");
        AtomicInteger changes = new AtomicInteger();
        book.addChangeListener(changed -> changes.incrementAndGet());

        book.deleteAll(List.of(bosses), List.of(vorkath));

        assertEquals(List.of(GearSetupBook.DEFAULT_SECTION_NAME), sectionNames());
        assertEquals(List.of("Zulrah"), namesIn(first));
        assertEquals(1, changes.get());
    }

    @Test
    void deleteAllIgnoresSetupsInsideDeletedSections() {
        SectionId bosses = book.addSection("Bosses").id();
        SetupId jad = add(bosses, "Jad");

        book.deleteAll(List.of(bosses), List.of(jad));

        assertEquals(1, book.sectionCount());
        assertEquals(0, book.setupCount());
    }

    @Test
    void deletingEverySectionLeavesAFreshDefaultOne() {
        SectionId bosses = book.addSection("Bosses").id();
        add(first, "Vorkath");

        book.deleteAll(List.of(first, bosses), List.of());

        assertEquals(1, book.sectionCount());
        assertEquals(GearSetupBook.DEFAULT_SECTION_NAME, book.sections().get(0).name());
        assertTrue(book.isEmpty());
    }

    @Test
    void deleteAllWithNothingSelectedDoesNotFireAChange() {
        AtomicInteger changes = new AtomicInteger();
        book.addChangeListener(changed -> changes.incrementAndGet());
        book.deleteAll(List.of(), List.of());
        assertEquals(0, changes.get());
    }

    @Test
    void addsRenamesAndRemovesSections() {
        SectionId bosses = book.addSection("Bosses").id();
        book.renameSection(bosses, "Boss gear");
        assertEquals("Boss gear", book.section(bosses).orElseThrow().name());

        book.removeSection(bosses);
        assertEquals(1, book.sectionCount());
    }

    @Test
    void removingASectionRemovesItsSetups() {
        SectionId bosses = book.addSection("Bosses").id();
        SetupId zulrah = add(bosses, "Zulrah");
        book.removeSection(bosses);
        assertTrue(book.setup(zulrah).isEmpty());
        assertEquals(0, book.setupCount());
    }

    @Test
    void refusesToRemoveTheLastSection() {
        assertThrows(IllegalStateException.class, () -> book.removeSection(first));
    }

    @Test
    void rejectsUnknownIds() {
        SetupId stranger = SetupId.random();
        assertThrows(IllegalArgumentException.class, () -> book.removeSetup(stranger));
        assertThrows(IllegalArgumentException.class, () -> book.addSetup(SectionId.random(), "x"));
    }

    @Test
    void notifiesListenersOnEveryChange() {
        AtomicInteger changes = new AtomicInteger();
        book.addChangeListener(changed -> changes.incrementAndGet());

        SetupId id = add(first, "Vorkath");
        book.change(id, setup -> setup.withName("Vorkath range").withoutIcon());
        book.addSection("Bosses");
        book.removeSetup(id);

        assertEquals(4, changes.get());
    }

    @Test
    void sectionsSnapshotIsImmutable() {
        assertThrows(UnsupportedOperationException.class, () -> book.sections().clear());
    }

    @Test
    void duplicatesLandRightAfterTheOriginalWithAFreshName() {
        GearSetupBook book = new GearSetupBook();
        SectionId section = book.sections().get(0).id();
        GearSetup vorkath = book.addSetup(section, "Vorkath");
        book.addSetup(section, "Zulrah");

        GearSetup copy = book.duplicateSetup(vorkath.id());

        assertEquals(List.of("Vorkath", "Vorkath (2)", "Zulrah"),
                book.sections().get(0).setups().stream().map(GearSetup::name).collect(Collectors.toList()));
        assertEquals(vorkath.content(), copy.content());
        assertNotEquals(vorkath.id(), copy.id());
    }

    @Test
    void sectionsCanBeInsertedAtAPosition() {
        GearSetupBook book = new GearSetupBook();
        book.insertSection(0, "First");
        book.insertSection(99, "Last");
        assertEquals(List.of("First", GearSetupBook.DEFAULT_SECTION_NAME, "Last"),
                book.sections().stream().map(GearSection::name).collect(Collectors.toList()));
    }

    @Test
    void aChangeThatChangesNothingDoesNotFire() {
        GearSetupBook book = new GearSetupBook();
        GearSetup setup = book.addSetup(book.sections().get(0).id(), "Vorkath");
        List<Integer> fired = new ArrayList<>();
        book.addChangeListener(changed -> fired.add(1));

        book.changeMeta(setup.id(), meta -> meta.withPinned(false));
        assertTrue(fired.isEmpty());

        book.changeMeta(setup.id(), meta -> meta.withPinned(true));
        assertEquals(1, fired.size());
        assertEquals(List.of(setup.id()), book.pinnedSetups().stream().map(GearSetup::id).collect(Collectors.toList()));
        assertThrows(IllegalArgumentException.class, () -> book.change(setup.id(), s -> GearSetup.named("Other")));
    }

    @Test
    void aSectionCanBeSortedIntoANewManualOrder() {
        GearSetupBook book = new GearSetupBook();
        SectionId section = book.sections().get(0).id();
        book.addSetup(section, "zulrah");
        book.addSetup(section, GearSetup.named("Bank").withContent(BankContent.empty()));
        book.addSetup(section, "Alpha");
        List<Integer> fired = new ArrayList<>();
        book.addChangeListener(changed -> fired.add(1));

        book.sortSection(section, SetupOrder.NAME.comparator());
        assertEquals(List.of("Alpha", "Bank", "zulrah"), book.sections().get(0).setups().stream().map(GearSetup::name).collect(Collectors.toList()));
        assertEquals(1, fired.size());

        book.sortSection(section, SetupOrder.NAME.comparator());
        assertEquals(1, fired.size(), "an unchanged order fires nothing");

        book.sortSection(section, SetupOrder.TYPE.comparator());
        assertEquals(List.of("Alpha", "zulrah", "Bank"), book.sections().get(0).setups().stream().map(GearSetup::name).collect(Collectors.toList()));
    }
    @Test
    void subSectionsSitUnderTheirParentAndMoveWithIt() {
        SectionId raids = book.addSection("Raids").id();
        SectionId toa = book.addSubSection(raids, "ToA").id();
        SectionId slayer = book.addSection("Slayer").id();
        SectionId cox = book.addSubSection(raids, "CoX").id();
        assertEquals(List.of("Setups", "Raids", "ToA", "CoX", "Slayer"), sectionNames());
        assertEquals(Optional.of(raids), book.section(cox).orElseThrow().parent());

        assertTrue(book.moveSectionUp(raids));
        assertEquals(List.of("Raids", "ToA", "CoX", "Setups", "Slayer"), sectionNames(), "the family moves as one");
        assertTrue(book.moveSectionDown(toa));
        assertEquals(List.of("Raids", "CoX", "ToA", "Setups", "Slayer"), sectionNames(), "children reorder among siblings");
        assertFalse(book.moveSectionDown(toa), "a child stays inside its family");
        assertTrue(book.moveSection(slayer, 0));
        assertEquals(List.of("Slayer", "Raids", "CoX", "ToA", "Setups"), sectionNames());
    }

    @Test
    void sectionsNestOneLevelDeepAndUnnestAfterTheirFamily() {
        SectionId raids = book.addSection("Raids").id();
        SectionId toa = book.addSection("ToA").id();
        book.nestSection(toa, raids);
        assertEquals(List.of("Setups", "Raids", "ToA"), sectionNames());
        assertTrue(book.section(toa).orElseThrow().isChildOf(raids));

        assertThrows(IllegalArgumentException.class, () -> book.nestSection(first, toa), "no grandchildren");
        assertThrows(IllegalArgumentException.class, () -> book.nestSection(raids, first), "a parent cannot be nested");
        assertThrows(IllegalArgumentException.class, () -> book.nestSection(raids, raids));

        book.unnestSection(toa);
        assertEquals(List.of("Setups", "Raids", "ToA"), sectionNames());
        assertFalse(book.section(toa).orElseThrow().isChild());
        assertTrue(book.moveSectionUp(toa));
        assertEquals(List.of("Setups", "ToA", "Raids"), sectionNames());
    }

    @Test
    void removingAParentLiftsItsChildren() {
        SectionId raids = book.addSection("Raids").id();
        SectionId toa = book.addSubSection(raids, "ToA").id();
        add(toa, "Expert");
        book.removeSection(raids);
        assertEquals(List.of("Setups", "ToA"), sectionNames());
        assertFalse(book.section(toa).orElseThrow().isChild());
        assertEquals(List.of("Expert"), namesIn(toa));

        SectionId slayer = book.addSection("Slayer").id();
        SectionId turael = book.addSubSection(slayer, "Turael").id();
        book.deleteAll(List.of(slayer), List.of());
        assertFalse(book.section(turael).orElseThrow().isChild());
    }

    @Test
    void peersAreInsertedBesideChildrenAndFamiliesStayWholeOnInsert() {
        SectionId raids = book.addSection("Raids").id();
        SectionId toa = book.addSubSection(raids, "ToA").id();
        SectionId before = book.insertSectionBefore(toa, "Before").id();
        SectionId after = book.insertSectionAfter(toa, "After").id();
        assertEquals(List.of("Setups", "Raids", "Before", "ToA", "After"), sectionNames());
        assertTrue(book.section(before).orElseThrow().isChildOf(raids));
        assertTrue(book.section(after).orElseThrow().isChildOf(raids));

        book.insertSectionAfter(raids, "Next");
        assertEquals(List.of("Setups", "Raids", "Before", "ToA", "After", "Next"), sectionNames(), "after a parent means after its children");
        assertEquals("Split", book.insertSection(2, "Split").name());
        assertEquals(List.of("Setups", "Raids", "Before", "ToA", "After", "Split", "Next"), sectionNames(), "an index inside a family lands after it");
    }

    @Test
    void changesNeedTheDispatchThreadWhileReadsDoNot() {
        GearSetupBook offThread = new GearSetupBook("Setups", () -> false);

        assertThrows(IllegalStateException.class, () -> offThread.addSetup("Vorkath"));
        assertThrows(IllegalStateException.class, () -> offThread.addSection("Raids"));
        assertEquals(1, offThread.sectionCount(), "reading is allowed from any thread");
        assertTrue(offThread.isEmpty(), "and the refused change left nothing behind");
    }

    @Test
    void readsAreServedFromASnapshotThatLaterChangesDoNotTouch() {
        List<GearSection> before = book.sections();
        SetupId added = book.addSetup("Vorkath").id();

        assertEquals(0, before.get(0).size(), "the snapshot taken earlier still reads as it did");
        assertEquals(1, book.sections().get(0).size());
        assertTrue(book.setup(added).isPresent());
        assertThrows(UnsupportedOperationException.class, () -> book.sections().add(GearSection.named("Nope")),
                "the snapshot cannot be edited by a reader");
    }

}
