package dev.dutchy.runelite.gear.content;

/** What one cell of a custom layout holds. */
public enum CellKind {

    EMPTY("Empty"),
    EQUIPMENT("Equipment"),
    INVENTORY("Inventory");

    private final String displayName;

    CellKind(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }
}
