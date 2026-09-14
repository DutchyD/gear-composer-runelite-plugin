package dev.dutchy.runelite.gear.content;

public enum EquipmentSlot {

    HEAD("Head", 1, 0),
    CAPE("Cape", 0, 1),
    AMULET("Amulet", 1, 1),
    AMMUNITION("Ammunition", 2, 1),
    WEAPON("Weapon", 0, 2),
    BODY("Body", 1, 2),
    SHIELD("Shield", 2, 2),
    LEGS("Legs", 1, 3),
    HANDS("Hands", 0, 4),
    FEET("Feet", 1, 4),
    RING("Ring", 2, 4);

    public static final int COLUMNS = 3;
    public static final int ROWS = 5;

    private final String displayName;
    private final int column;
    private final int row;

    EquipmentSlot(String displayName, int column, int row) {
        this.displayName = displayName;
        this.column = column;
        this.row = row;
    }

    public String displayName() {
        return displayName;
    }

    public int column() {
        return column;
    }

    public int row() {
        return row;
    }
}
