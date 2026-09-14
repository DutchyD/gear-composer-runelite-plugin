package dev.dutchy.runelite.gear.ui;

import dev.dutchy.runelite.gear.GearSetupBook;
import dev.dutchy.runelite.gear.SetupId;
import dev.dutchy.runelite.gear.activation.SetupActivator;
import dev.dutchy.runelite.gear.bank.ActiveSetup;
import dev.dutchy.runelite.gear.bank.BankLayoutApplier;
import dev.dutchy.runelite.gear.content.SetupType;
import dev.dutchy.runelite.gear.guide.GuideId;
import dev.dutchy.runelite.gear.guide.GuideProgress;
import dev.dutchy.runelite.gear.guide.SampleSetups;
import dev.dutchy.runelite.gear.history.SetupHistory;

import javax.inject.Inject;
import java.util.Objects;

/**
 * The throwaway setup a tutorial walks on. It is made when the guide starts and removed when it
 * ends unless the player keeps it, and it is what a guide shows in the bank so there is always
 * something to point at.
 */
public final class GuideSamples {

    static final String KEEP_QUESTION = "Keep the sample setup to start from?";

    private final GearSetupBook book;
    private final SetupHistory history;
    private final SetupActivator activator;
    private final ActiveSetup activeSetup;
    private final BankLayoutApplier bank;
    private final GuideProgress progress;

    private Prompts prompts = SilentPrompts.INSTANCE;
    private Runnable onBankChanged = () -> {
    };

    @Inject
    public GuideSamples(GearSetupBook book, SetupHistory history, SetupActivator activator, ActiveSetup activeSetup,
                        BankLayoutApplier bank, GuideProgress progress) {
        this.book = Objects.requireNonNull(book, "book");
        this.history = Objects.requireNonNull(history, "history");
        this.activator = Objects.requireNonNull(activator, "activator");
        this.activeSetup = Objects.requireNonNull(activeSetup, "activeSetup");
        this.bank = Objects.requireNonNull(bank, "bank");
        this.progress = Objects.requireNonNull(progress, "progress");
    }

    /** Wired by the sidebar, which owns the dialogs and the marks on the tiles. */
    void shownBy(Prompts newPrompts, Runnable bankChanged) {
        this.prompts = Objects.requireNonNull(newPrompts, "newPrompts");
        this.onBankChanged = Objects.requireNonNull(bankChanged, "bankChanged");
    }

    public SetupId create(SetupType type) {
        return book.addSetup(book.sections().get(0).id(), SampleSetups.of(type)).id();
    }

    /** Takes the sample and its history away, unless something already removed it. */
    public void remove(SetupId sample) {
        if (book.setup(sample).isPresent()) {
            book.removeSetup(sample);
            history.forget(sample);
        }
    }

    /** Turns the sample into an ordinary setup the player can build on. */
    public void keep(SetupId sample) {
        if (book.setup(sample).isPresent()) {
            book.change(sample, SampleSetups::kept);
        }
    }

    public boolean isBankOpen() {
        return bank.isBankOpen();
    }

    public void showInBank(SetupId sample) {
        activator.show(sample);
        onBankChanged.run();
    }

    public void hideFromBank(SetupId sample) {
        if (activeSetup.isActive(sample)) {
            activator.clear();
            onBankChanged.run();
        }
    }

    public boolean askToKeep() {
        return prompts.confirm(KEEP_QUESTION, "Tutorial finished");
    }

    /** Whether any tutorial has been through to the end, which is when the front door stops offering one. */
    public boolean anyTutorialCompleted() {
        return progress.completed().stream().anyMatch(GuideId::isTutorial);
    }
}
