package dev.dutchy.runelite.gear.bank;

/** Bank item grid measurements, matching the values the game's own bank scripts use, plus the header strip dividers add to a row. */
public final class BankGeometry {

    public static final int ITEM_WIDTH = 36;
    public static final int ITEM_HEIGHT = 32;
    public static final int ITEM_X_PADDING = 12;
    public static final int ITEM_Y_PADDING = 4;
    public static final int ITEM_START_X = 51;
    /** Room for one line of the small font above a row's items. */
    public static final int HEADER_HEIGHT = 14;
    /** Room for the row of variant tabs above a layout. */
    public static final int VARIANT_STRIP_HEIGHT = 24;

    private BankGeometry() {
    }

    public static int x(int slotIndex) {
        return slotIndex % BankGrid.ITEMS_PER_ROW * (ITEM_WIDTH + ITEM_X_PADDING) + ITEM_START_X;
    }

    /** The top of a slot's row when no row carries a header. */
    public static int y(int slotIndex) {
        return slotIndex / BankGrid.ITEMS_PER_ROW * (ITEM_HEIGHT + ITEM_Y_PADDING);
    }
}
