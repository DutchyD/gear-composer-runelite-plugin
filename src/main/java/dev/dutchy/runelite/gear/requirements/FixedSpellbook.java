package dev.dutchy.runelite.gear.requirements;

import dev.dutchy.runelite.gear.Spellbook;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

/** A spellbook that only changes when told to; for tests and previews. */
public final class FixedSpellbook implements CurrentSpellbook {

    private final List<Runnable> listeners = new CopyOnWriteArrayList<>();
    private Spellbook current;

    public FixedSpellbook(Spellbook current) {
        this.current = requireReal(current);
    }

    public void set(Spellbook spellbook) {
        current = requireReal(spellbook);
        listeners.forEach(Runnable::run);
    }

    public void logOut() {
        current = null;
        listeners.forEach(Runnable::run);
    }

    @Override
    public Optional<Spellbook> current() {
        return Optional.ofNullable(current);
    }

    @Override
    public void addListener(Runnable listener) {
        listeners.add(Objects.requireNonNull(listener, "listener"));
    }

    private static Spellbook requireReal(Spellbook spellbook) {
        if (!Objects.requireNonNull(spellbook, "spellbook").isRequirement()) {
            throw new IllegalArgumentException("The game is always on a real spellbook");
        }
        return spellbook;
    }
}
