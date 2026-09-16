package dev.dutchy.runelite.gear;

import java.util.Optional;

/** The spellbooks a setup can require, by the value the game reports; {@link #ANY} means no requirement. */
public enum Spellbook {
    ANY(-1, "Any"),
    STANDARD(0, "Standard"),
    ANCIENT(1, "Ancients"),
    LUNAR(2, "Lunar"),
    ARCEUUS(3, "Arceuus");

    private final int gameValue;
    private final String displayName;

    Spellbook(int gameValue, String displayName) {
        this.gameValue = gameValue;
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }

    public boolean isRequirement() {
        return this != ANY;
    }

    public static Optional<Spellbook> fromGameValue(int value) {
        for (Spellbook spellbook : values()) {
            if (spellbook.isRequirement() && spellbook.gameValue == value) {
                return Optional.of(spellbook);
            }
        }
        return Optional.empty();
    }
}
