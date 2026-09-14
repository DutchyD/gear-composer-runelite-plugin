package dev.dutchy.runelite.gear.ui;

import dev.dutchy.runelite.gear.GearSection;
import dev.dutchy.runelite.gear.GearSetup;
import dev.dutchy.runelite.gear.GearSetupBook;
import dev.dutchy.runelite.gear.SectionId;
import dev.dutchy.runelite.gear.SetupId;
import dev.dutchy.runelite.gear.content.GearContent;
import dev.dutchy.runelite.gear.content.EquipmentSlot;
import dev.dutchy.runelite.gear.content.SetupItem;
import dev.dutchy.runelite.gear.history.InMemoryHistoryStore;
import dev.dutchy.runelite.gear.history.SetupHistory;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BulkDeleteTest {

    private final GearSetupBook book = new GearSetupBook();
    private final SetupHistory history = new SetupHistory(book, new InMemoryHistoryStore(),
            Clock.fixed(Instant.parse("2026-09-08T10:00:00Z"), ZoneOffset.UTC));
    private final RecordingPrompts prompts = new RecordingPrompts();
    private final BulkDelete bulk = new BulkDelete(book, history);
    private final List<String> events = new ArrayList<>();

    private final SectionId first = book.sections().get(0).id();

    BulkDeleteTest() {
        bulk.shownBy(prompts, new BulkDelete.Listener() {
            @Override
            public void modeChanged() {
                events.add("mode");
            }

            @Override
            public void selectionChanged() {
                events.add("selection");
            }
        }, this::order);
    }

    /** The list as it is rendered: every section followed by its setups. */
    private List<BulkTarget> order() {
        List<BulkTarget> targets = new ArrayList<>();
        for (GearSection section : book.sections()) {
            targets.add(BulkTarget.of(section.id()));
            section.setups().forEach(setup -> targets.add(BulkTarget.of(setup.id())));
        }
        return targets;
    }

    private SetupId add(SectionId section, String name) {
        return book.addSetup(section, name).id();
    }

    private List<String> setupNames() {
        return book.sections().stream().flatMap(section -> section.setups().stream())
                .map(GearSetup::name).collect(Collectors.toList());
    }

    @Test
    void turningTheModeOnSaysHowToPickAndOffForgetsWhatWasPicked() {
        SetupId vorkath = add(first, "Vorkath");

        bulk.set(true);
        bulk.select(BulkTarget.of(vorkath), false);

        assertTrue(bulk.isOn());
        assertEquals(1, bulk.count());
        assertEquals(BulkDelete.PICK_HINT, prompts.lastMessage());

        bulk.set(false);

        assertFalse(bulk.isOn());
        assertEquals(0, bulk.count());
        assertEquals(List.of("Vorkath"), setupNames(), "leaving the mode deletes nothing");
    }

    @Test
    void aClickPicksOneAndClickingItAgainPutsItBack() {
        SetupId vorkath = add(first, "Vorkath");
        bulk.set(true);

        bulk.select(BulkTarget.of(vorkath), false);
        assertTrue(bulk.isSelected(BulkTarget.of(vorkath)));
        assertEquals("1 selected", bulk.countText());

        bulk.select(BulkTarget.of(vorkath), false);
        assertFalse(bulk.isSelected(BulkTarget.of(vorkath)));
        assertEquals("0 selected", bulk.countText());
    }

    @Test
    void deletingTakesTheSectionsSetupsAndTheirHistoryWithIt() {
        SectionId skilling = book.addSection("Skilling").id();
        SetupId vorkath = add(first, "Vorkath");
        add(first, "Zulrah");
        SetupId wintertodt = add(skilling, "Wintertodt");
        history.applyContent(wintertodt, GearContent.empty().withEquipped(EquipmentSlot.WEAPON, SetupItem.of(4151)), "Edited");
        assertEquals(1, history.revisions(wintertodt).size());

        bulk.set(true);
        bulk.select(BulkTarget.of(vorkath), false);
        bulk.select(BulkTarget.of(skilling), false);
        bulk.deleteSelected();

        assertEquals(List.of("Zulrah"), setupNames());
        assertEquals(1, book.sectionCount(), "the section went with its setups");
        assertTrue(history.revisions(wintertodt).isEmpty(), "and so did their history");
        assertFalse(bulk.isOn(), "the mode ends after deleting");
        assertEquals("Deleted 2 items", prompts.lastMessage());
    }

    @Test
    void oneItemIsReportedInTheSingular() {
        SetupId vorkath = add(first, "Vorkath");

        bulk.set(true);
        bulk.select(BulkTarget.of(vorkath), false);
        bulk.deleteSelected();

        assertEquals("Deleted 1 item", prompts.lastMessage());
    }

    @Test
    void aShiftClickPicksEverythingBetweenAcrossSectionsAndSetups() {
        SectionId skilling = book.addSection("Skilling").id();
        add(first, "Vorkath");
        SetupId zulrah = add(first, "Zulrah");
        add(skilling, "Wintertodt");

        bulk.set(true);
        bulk.select(BulkTarget.of(zulrah), false);
        bulk.select(BulkTarget.of(skilling), true);
        bulk.deleteSelected();

        assertEquals(List.of("Vorkath"), setupNames());
        assertEquals(1, book.sectionCount());
    }

    @Test
    void nothingHappensWhenTheQuestionIsAnsweredNo() {
        SetupId vorkath = add(first, "Vorkath");
        prompts.answering(false);

        bulk.set(true);
        bulk.select(BulkTarget.of(vorkath), false);
        bulk.deleteSelected();

        assertEquals(List.of("Vorkath"), setupNames());
        assertTrue(bulk.isOn(), "the mode stays on so the pick can be changed");
    }

    @Test
    void withNothingPickedTheQuestionIsNotEvenAsked() {
        bulk.set(true);
        bulk.deleteSelected();

        assertTrue(prompts.questions().isEmpty());
    }

    @Test
    void somethingDeletedElsewhereLeavesThePickAlone() {
        SetupId vorkath = add(first, "Vorkath");
        SetupId zulrah = add(first, "Zulrah");
        bulk.set(true);
        bulk.select(BulkTarget.of(vorkath), false);
        bulk.select(BulkTarget.of(zulrah), false);

        book.removeSetup(zulrah);
        bulk.dropMissing();
        bulk.deleteSelected();

        assertTrue(setupNames().isEmpty());
        assertEquals("Deleted 1 item", prompts.lastMessage(), "the one already gone was not counted");
    }

    @Test
    void theListIsToldWhichWayItNeedsToRedraw() {
        SetupId vorkath = add(first, "Vorkath");

        bulk.set(true);
        bulk.select(BulkTarget.of(vorkath), false);

        assertEquals(List.of("mode", "selection"), events);
    }
}
