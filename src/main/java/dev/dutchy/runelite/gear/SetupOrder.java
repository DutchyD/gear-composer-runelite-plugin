package dev.dutchy.runelite.gear;

import java.util.Comparator;

/** The orders a section can be sorted into. */
public enum SetupOrder {
    NAME("Sort by name", Comparator.comparing((GearSetup setup) -> setup.name().toLowerCase())),
    TYPE("Sort by layout", Comparator.comparing(GearSetup::type).thenComparing(setup -> setup.name().toLowerCase()));

    private final String displayName;
    private final Comparator<GearSetup> comparator;

    SetupOrder(String displayName, Comparator<GearSetup> comparator) {
        this.displayName = displayName;
        this.comparator = comparator;
    }

    public String displayName() {
        return displayName;
    }

    public Comparator<GearSetup> comparator() {
        return comparator;
    }
}
