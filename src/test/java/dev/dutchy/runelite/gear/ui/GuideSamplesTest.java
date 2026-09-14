package dev.dutchy.runelite.gear.ui;

import dev.dutchy.runelite.gear.GearSetup;
import dev.dutchy.runelite.gear.GearSetupBook;
import dev.dutchy.runelite.gear.SetupId;
import dev.dutchy.runelite.gear.activation.SetupActivator;
import dev.dutchy.runelite.gear.bank.ActiveSetup;
import dev.dutchy.runelite.gear.bank.BankLayout;
import dev.dutchy.runelite.gear.bank.BankLayoutApplier;
import dev.dutchy.runelite.gear.bank.BankLayoutPlanner;
import dev.dutchy.runelite.gear.content.GearContent;
import dev.dutchy.runelite.gear.content.EquipmentSlot;
import dev.dutchy.runelite.gear.content.SetupItem;
import dev.dutchy.runelite.gear.content.SetupType;
import dev.dutchy.runelite.gear.guide.GuideId;
import dev.dutchy.runelite.gear.guide.GuideProgress;
import dev.dutchy.runelite.gear.guide.SampleSetups;
import dev.dutchy.runelite.gear.history.InMemoryHistoryStore;
import dev.dutchy.runelite.gear.history.SetupHistory;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GuideSamplesTest {

    private final GearSetupBook book = new GearSetupBook();
    private final SetupHistory history = new SetupHistory(book, new InMemoryHistoryStore(),
            Clock.fixed(Instant.parse("2026-09-08T10:00:00Z"), ZoneOffset.UTC));
    private final ActiveSetup activeSetup = new ActiveSetup();
    private final List<BankLayout> applied = new ArrayList<>();
    private int cleared;
    private boolean bankOpen = true;
    private final List<String> redraws = new ArrayList<>();
    private final RecordingPrompts prompts = new RecordingPrompts();
    private final GuideProgress progress = GuideProgress.inMemory();

    private final BankLayoutApplier bank = new BankLayoutApplier() {
        @Override
        public void apply(BankLayout layout) {
            applied.add(layout);
        }

        @Override
        public void clear() {
            cleared++;
        }

        @Override
        public boolean isBankOpen() {
            return bankOpen;
        }
    };

    private final GuideSamples samples = new GuideSamples(book, history,
            new SetupActivator(book, activeSetup, new BankLayoutPlanner(), bank), activeSetup, bank, progress);

    GuideSamplesTest() {
        samples.shownBy(prompts, () -> redraws.add("redraw"));
    }

    @Test
    void aSampleIsMadeInTheFirstSectionAndIsMarkedAsOne() {
        SetupId sample = samples.create(SetupType.GEAR);

        assertTrue(SampleSetups.isSample(book.setup(sample).orElseThrow()));
        assertEquals(1, book.sections().get(0).setups().size());
    }

    @Test
    void removingASampleTakesItsHistoryWithIt() {
        SetupId sample = samples.create(SetupType.GEAR);
        history.applyContent(sample, GearContent.empty().withEquipped(EquipmentSlot.WEAPON, SetupItem.of(4151)), "Edited");

        samples.remove(sample);

        assertTrue(book.setup(sample).isEmpty());
        assertTrue(history.revisions(sample).isEmpty());
    }

    @Test
    void removingOneThatIsAlreadyGoneDoesNothing() {
        SetupId sample = samples.create(SetupType.GEAR);
        book.removeSetup(sample);

        samples.remove(sample);

        assertTrue(book.setup(sample).isEmpty());
    }

    @Test
    void keepingASampleTurnsItIntoAnOrdinarySetup() {
        SetupId sample = samples.create(SetupType.GEAR);

        samples.keep(sample);

        GearSetup kept = book.setup(sample).orElseThrow();
        assertFalse(SampleSetups.isSample(kept), "it is the player's now");
    }

    @Test
    void aSampleIsShownInTheBankAndTakenDownAgain() {
        SetupId sample = samples.create(SetupType.GEAR);

        samples.showInBank(sample);

        assertTrue(activeSetup.isActive(sample));
        assertEquals(1, applied.size());
        assertEquals(List.of("redraw"), redraws);

        samples.hideFromBank(sample);

        assertFalse(activeSetup.isActive(sample));
        assertEquals(1, cleared);
        assertEquals(2, redraws.size());
    }

    @Test
    void hidingSomethingThatWasNeverShownLeavesTheBankAlone() {
        SetupId sample = samples.create(SetupType.GEAR);

        samples.hideFromBank(sample);

        assertEquals(0, cleared);
        assertTrue(redraws.isEmpty());
    }

    @Test
    void whetherTheBankIsOpenComesFromTheBankItself() {
        assertTrue(samples.isBankOpen());

        bankOpen = false;

        assertFalse(samples.isBankOpen());
    }

    @Test
    void theQuestionAtTheEndIsWhetherToKeepTheSample() {
        prompts.answering(true);
        assertTrue(samples.askToKeep());
        assertEquals(List.of(GuideSamples.KEEP_QUESTION), prompts.questions());

        prompts.answering(false);
        assertFalse(samples.askToKeep());
    }

    @Test
    void theFrontDoorRetiresOnceATutorialHasBeenThrough() {
        assertFalse(samples.anyTutorialCompleted());

        progress.markCompleted(GuideId.LIST);
        assertFalse(samples.anyTutorialCompleted(), "a page guide is not a tutorial");

        progress.markCompleted(GuideId.TUTORIAL_GEAR);
        assertTrue(samples.anyTutorialCompleted());
    }
}
