package dev.dutchy.runelite.gear.bank;

import java.util.Objects;

/** Which numbered tab a bank slot sits in, given how many items each tab holds. */
public final class BankTabs {

    public static final int TAB_COUNT = 9;
    public static final int MAIN_TAB = 0;

    private BankTabs() {
    }

    /**
     * The bank stores tabs one after another, tab 1 first, with the unfiled main tab after the last
     * numbered tab. Returns 1 to 9 for a numbered tab and {@link #MAIN_TAB} otherwise.
     */
    public static int tabOf(int bankIndex, int[] tabCounts) {
        Objects.requireNonNull(tabCounts, "tabCounts");
        if (bankIndex < 0) {
            throw new IllegalArgumentException("Bank index must be non-negative, got " + bankIndex);
        }
        int start = 0;
        for (int tab = 0; tab < Math.min(tabCounts.length, TAB_COUNT); tab++) {
            int end = start + Math.max(0, tabCounts[tab]);
            if (bankIndex < end) {
                return tab + 1;
            }
            start = end;
        }
        return MAIN_TAB;
    }
}
