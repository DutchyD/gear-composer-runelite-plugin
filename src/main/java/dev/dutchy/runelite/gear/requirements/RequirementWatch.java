package dev.dutchy.runelite.gear.requirements;

import dev.dutchy.runelite.gear.*;
import dev.dutchy.runelite.gear.bank.ActiveSetup;

import javax.inject.Inject;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/** Compares the active setup's requirements with the game and words a warning when they differ. */
public final class RequirementWatch {

    private final GearSetupBook book;
    private final ActiveSetup active;
    private final CurrentSpellbook spellbook;
    private final CurrentQuickPrayers quickPrayers;
    private final List<Runnable> listeners = new CopyOnWriteArrayList<>();

    @Inject
    public RequirementWatch(GearSetupBook book, ActiveSetup active, CurrentSpellbook spellbook, CurrentQuickPrayers quickPrayers) {
        this.book = Objects.requireNonNull(book, "book");
        this.active = Objects.requireNonNull(active, "active");
        this.spellbook = Objects.requireNonNull(spellbook, "spellbook");
        this.quickPrayers = Objects.requireNonNull(quickPrayers, "quickPrayers");
        book.addChangeListener(changed -> notifyListeners());
        active.addListener(this::notifyListeners);
        spellbook.addListener(this::notifyListeners);
        quickPrayers.addListener(this::notifyListeners);
    }

    /** Every mismatch to show, joined; the spellbook first. */
    public Optional<String> warning() {
        List<String> warnings = new ArrayList<>();
        spellbookWarning().ifPresent(warnings::add);
        quickPrayerWarning().ifPresent(warnings::add);
        return warnings.isEmpty() ? Optional.empty() : Optional.of(String.join(" · ", warnings));
    }

    /** Such as "Zulrah needs Lunar, you are on Standard". */
    public Optional<String> spellbookWarning() {
        Optional<GearSetup> setup = active.current().flatMap(book::setup);
        Optional<Spellbook> current = spellbook.current();
        if (setup.isEmpty() || current.isEmpty()) {
            return Optional.empty();
        }
        Spellbook required = setup.get().meta().requirements().spellbook();
        if (!required.isRequirement() || required == current.get()) {
            return Optional.empty();
        }
        return Optional.of(setup.get().name() + " needs " + required.displayName() + ", you are on " + current.get().displayName());
    }

    /** Such as "Quick prayers differ: set Piety, Protect from Melee". */
    public Optional<String> quickPrayerWarning() {
        Optional<GearSetup> setup = active.current().flatMap(book::setup);
        Optional<Set<Prayer>> current = quickPrayers.selected();
        if (setup.isEmpty() || current.isEmpty()) {
            return Optional.empty();
        }
        Requirements required = setup.get().meta().requirements();
        if (!required.hasQuickPrayers() || required.quickPrayers().equals(current.get())) {
            return Optional.empty();
        }
        return Optional.of("Quick prayers differ: set " + Prayer.describe(required.quickPrayers()));
    }

    public void addListener(Runnable listener) {
        listeners.add(Objects.requireNonNull(listener, "listener"));
    }

    private void notifyListeners() {
        listeners.forEach(Runnable::run);
    }
}
