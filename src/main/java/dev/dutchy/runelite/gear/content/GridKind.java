package dev.dutchy.runelite.gear.content;

public enum GridKind {

    INVENTORY("Inventory"),
    LEFT("Left"),
    RIGHT("Right");

    private final String displayName;

    GridKind(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }
}
