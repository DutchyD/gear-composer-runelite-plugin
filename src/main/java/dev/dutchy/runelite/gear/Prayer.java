package dev.dutchy.runelite.gear;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/** The prayer book in its in-game order; the bit is the prayer's place in the quick-prayer selection. */
public enum Prayer {

    THICK_SKIN("Thick Skin", 0),
    BURST_OF_STRENGTH("Burst of Strength", 1),
    CLARITY_OF_THOUGHT("Clarity of Thought", 2),
    ROCK_SKIN("Rock Skin", 3),
    SUPERHUMAN_STRENGTH("Superhuman Strength", 4),
    IMPROVED_REFLEXES("Improved Reflexes", 5),
    RAPID_RESTORE("Rapid Restore", 6),
    RAPID_HEAL("Rapid Heal", 7),
    PROTECT_ITEM("Protect Item", 8),
    STEEL_SKIN("Steel Skin", 9),
    ULTIMATE_STRENGTH("Ultimate Strength", 10),
    INCREDIBLE_REFLEXES("Incredible Reflexes", 11),
    PROTECT_FROM_MAGIC("Protect from Magic", 12),
    PROTECT_FROM_MISSILES("Protect from Missiles", 13),
    PROTECT_FROM_MELEE("Protect from Melee", 14),
    RETRIBUTION("Retribution", 15),
    REDEMPTION("Redemption", 16),
    SMITE("Smite", 17),
    SHARP_EYE("Sharp Eye", 18),
    MYSTIC_WILL("Mystic Will", 19),
    HAWK_EYE("Hawk Eye", 20),
    MYSTIC_LORE("Mystic Lore", 21),
    EAGLE_EYE("Eagle Eye", 22),
    MYSTIC_MIGHT("Mystic Might", 23),
    CHIVALRY("Chivalry", 24),
    PIETY("Piety", 25),
    PRESERVE("Preserve", 26),
    RIGOUR("Rigour", 27),
    AUGURY("Augury", 28),
    DEADEYE("Deadeye", 29),
    MYSTIC_VIGOUR("Mystic Vigour", 30);

    public static final int COLUMNS = 5;

    private final String displayName;
    private final int quickBit;

    Prayer(String displayName, int quickBit) {
        this.displayName = displayName;
        this.quickBit = quickBit;
    }

    public String displayName() {
        return displayName;
    }

    /** The prayers whose bits are set in the game's quick-prayer selection value. */
    public static Set<Prayer> fromQuickPrayerBits(int bits) {
        Set<Prayer> selected = EnumSet.noneOf(Prayer.class);
        for (Prayer prayer : values()) {
            if ((bits & (1 << prayer.quickBit)) != 0) {
                selected.add(prayer);
            }
        }
        return selected;
    }

    public static int toQuickPrayerBits(Collection<Prayer> prayers) {
        int bits = 0;
        for (Prayer prayer : Objects.requireNonNull(prayers, "prayers")) {
            bits |= 1 << prayer.quickBit;
        }
        return bits;
    }

    /** Names in book order, joined for a sentence. */
    public static String describe(Collection<Prayer> prayers) {
        return EnumSet.copyOf(prayers).stream().map(Prayer::displayName).collect(Collectors.joining(", "));
    }
}
