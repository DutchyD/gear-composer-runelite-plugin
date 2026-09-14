package dev.dutchy.runelite.gear;

import lombok.Value;
import lombok.experimental.Accessors;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

/** What a setup needs beyond items: a spellbook and the quick prayers to have set. */
@Value
@Accessors(fluent = true)
public class Requirements {
    Spellbook spellbook;
    Set<Prayer> quickPrayers;

    private static final Requirements NONE = new Requirements(Spellbook.ANY, Set.of());

    public Requirements(Spellbook spellbook, Set<Prayer> quickPrayers) {
        this.spellbook = Objects.requireNonNull(spellbook, "spellbook");
        Objects.requireNonNull(quickPrayers, "quickPrayers");
        this.quickPrayers = quickPrayers.isEmpty() ? Set.of() : Collections.unmodifiableSet(EnumSet.copyOf(quickPrayers));
    }

    public static Requirements none() {
        return NONE;
    }

    public static Requirements ofSpellbook(Spellbook spellbook) {
        return new Requirements(spellbook, Set.of());
    }

    public boolean isEmpty() {
        return !spellbook.isRequirement() && quickPrayers.isEmpty();
    }

    public boolean hasQuickPrayers() {
        return !quickPrayers.isEmpty();
    }

    public Requirements withSpellbook(Spellbook newSpellbook) {
        return new Requirements(newSpellbook, quickPrayers);
    }

    public Requirements withQuickPrayers(Set<Prayer> newQuickPrayers) {
        return new Requirements(spellbook, newQuickPrayers);
    }
}
