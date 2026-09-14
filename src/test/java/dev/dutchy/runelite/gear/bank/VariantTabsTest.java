package dev.dutchy.runelite.gear.bank;

import dev.dutchy.runelite.gear.GearSetup;
import dev.dutchy.runelite.gear.content.GearContent;
import dev.dutchy.runelite.gear.content.SetupVariant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VariantTabsTest {

    private static final int WIDTH = 400;
    private static final int ARROWS = 2 * (VariantTabs.ARROW_WIDTH + VariantTabs.GAP);

    private static VariantTabs tabs(int count, int chosen) {
        List<String> names = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            names.add("V" + i);
        }
        return new VariantTabs(names, chosen);
    }

    @Test
    void aSetupWithOneVariantHasNoTabsAndTakesNoRoom() {
        VariantTabs tabs = VariantTabs.of(GearSetup.named("Solo"));
        assertTrue(tabs.isEmpty());
        assertEquals(0, tabs.height());
    }

    @Test
    void aSetupWithVariantsGetsATabEachWithTheChosenOneMarked() {
        GearSetup setup = GearSetup.named("Vorkath")
                .withAddedVariant(new SetupVariant("Mage", GearContent.empty()))
                .withAddedVariant(new SetupVariant("Range", GearContent.empty()))
                .withSelectedVariant(1);
        VariantTabs tabs = VariantTabs.of(setup);

        assertEquals(List.of("Default", "Mage", "Range"), tabs.names());
        assertEquals(1, tabs.chosen());
        assertEquals(BankGeometry.VARIANT_STRIP_HEIGHT, tabs.height());
        assertThrows(IllegalArgumentException.class, () -> new VariantTabs(List.of("A"), 1));
    }

    @Test
    void upToFourTabsShareTheWholeWidthWithoutArrows() {
        VariantTabs three = tabs(3, 0);
        assertFalse(three.overflows());
        assertEquals(3, three.visibleCount());
        int tab = (WIDTH - 2 * VariantTabs.GAP) / 3;
        assertEquals(tab, three.tabWidth(WIDTH));
        assertEquals(0, three.tabX(0, WIDTH));
        assertEquals(2 * (tab + VariantTabs.GAP), three.tabX(2, WIDTH));
        assertEquals(0, three.clampOffset(5), "nothing to scroll");
        assertEquals(4, tabs(4, 0).visibleCount());
        assertFalse(tabs(4, 0).overflows());
    }

    @Test
    void moreThanFourTabsShowFourBetweenArrowsAndKeepTheChosenOneInView() {
        VariantTabs eight = tabs(8, 6);
        assertTrue(eight.overflows());
        assertEquals(4, eight.visibleCount());
        int tab = (WIDTH - ARROWS - 3 * VariantTabs.GAP) / 4;
        assertEquals(tab, eight.tabWidth(WIDTH), "four tabs fill the room between the arrows");
        assertEquals(VariantTabs.ARROW_WIDTH + VariantTabs.GAP, eight.tabX(0, WIDTH));
        assertEquals(VariantTabs.ARROW_WIDTH + VariantTabs.GAP + 3 * (tab + VariantTabs.GAP), eight.tabX(3, WIDTH));
        assertEquals(4, eight.maxOffset(), "scrolling stops when the last tab is on show");
        assertEquals(4, eight.clampOffset(99));
        assertEquals(0, eight.clampOffset(-3));
        assertEquals(3, eight.offsetShowingChosen(0), "the chosen tab is brought into view from the right");
        assertEquals(4, eight.offsetShowingChosen(7), "and from the left");
        assertEquals(3, eight.offsetShowingChosen(3), "and left alone when already on show");
        assertEquals(1, tabs(8, 0).tabWidth(10), "a very narrow bank still gives each tab a pixel");
    }

    @Test
    void rowsStartBelowTheTabs() {
        BankRows rows = new BankRows(Set.of(0), BankGeometry.VARIANT_STRIP_HEIGHT);
        assertEquals(BankGeometry.VARIANT_STRIP_HEIGHT, rows.top(0));
        assertEquals(BankGeometry.VARIANT_STRIP_HEIGHT + BankGeometry.HEADER_HEIGHT, rows.itemY(0));
        assertEquals(BankGeometry.VARIANT_STRIP_HEIGHT, rows.height(0));
        assertEquals(BankGeometry.VARIANT_STRIP_HEIGHT + BankGeometry.HEADER_HEIGHT + 32, rows.height(1));
    }
}
