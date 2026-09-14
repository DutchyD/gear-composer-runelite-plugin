package dev.dutchy.runelite.gear.requirements;

import dev.dutchy.runelite.gear.Prayer;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/** A quick-prayer selection that only changes when told to; for tests and previews. */
public final class FixedQuickPrayers implements CurrentQuickPrayers {

    private final List<Runnable> listeners = new CopyOnWriteArrayList<>();
    private Set<Prayer> selected;

    public FixedQuickPrayers(Set<Prayer> selected) {
        this.selected = copy(selected);
    }

    public void set(Set<Prayer> newSelection) {
        selected = copy(newSelection);
        listeners.forEach(Runnable::run);
    }

    public void logOut() {
        selected = null;
        listeners.forEach(Runnable::run);
    }

    @Override
    public Optional<Set<Prayer>> selected() {
        return Optional.ofNullable(selected);
    }

    @Override
    public void addListener(Runnable listener) {
        listeners.add(Objects.requireNonNull(listener, "listener"));
    }

    private static Set<Prayer> copy(Set<Prayer> prayers) {
        Objects.requireNonNull(prayers, "prayers");
        return prayers.isEmpty() ? Set.of() : Set.copyOf(EnumSet.copyOf(prayers));
    }
}
