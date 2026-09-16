package dev.dutchy.runelite.gear.ui;

/** How the setup list draws its tiles. */
public enum TileStyle {
    GRID("Grid", 4),
    COMPACT("Compact", 6),
    LIST("List", 1);

    private final String displayName;
    private final int columns;

    TileStyle(String displayName, int columns) {
        this.displayName = displayName;
        this.columns = columns;
    }

    public String displayName() {
        return displayName;
    }

    public int columns() {
        return columns;
    }

    public TileStyle next() {
        TileStyle[] all = values();
        return all[(ordinal() + 1) % all.length];
    }
}
