package dev.dutchy.runelite.gear.bank;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BankTabsTest {

    @Test
    void indicesFallIntoTabsInOrderWithTheRestInTheMainTab() {
        int[] counts = {3, 0, 2};
        assertEquals(1, BankTabs.tabOf(0, counts));
        assertEquals(1, BankTabs.tabOf(2, counts));
        assertEquals(3, BankTabs.tabOf(3, counts), "an empty tab takes no slots");
        assertEquals(3, BankTabs.tabOf(4, counts));
        assertEquals(BankTabs.MAIN_TAB, BankTabs.tabOf(5, counts));
        assertEquals(BankTabs.MAIN_TAB, BankTabs.tabOf(0, new int[0]));
    }

    @Test
    void sizesNotReadYetPutEverythingInTheMainTab() {
        int[] unread = new int[BankTabs.TAB_COUNT];
        assertEquals(BankTabs.MAIN_TAB, BankTabs.tabOf(0, unread), "no badge is drawn before the sizes are known");
        assertEquals(BankTabs.MAIN_TAB, BankTabs.tabOf(500, unread));
    }
}
