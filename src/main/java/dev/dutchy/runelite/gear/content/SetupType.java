package dev.dutchy.runelite.gear.content;

public enum SetupType {

    GEAR("Gear Layout"),
    BANK("Bank Layout"),
    CUSTOM("Custom Layout");

    private final String displayName;

    SetupType(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }
}
