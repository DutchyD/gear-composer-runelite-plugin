package dev.dutchy.runelite.gear.ui;

import dev.dutchy.runelite.gear.SectionId;
import dev.dutchy.runelite.gear.SetupId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BulkSelectionTest {

    private static final BulkTarget SECTION_A = BulkTarget.of(SectionId.random());
    private static final BulkTarget SETUP_1 = BulkTarget.of(SetupId.random());
    private static final BulkTarget SETUP_2 = BulkTarget.of(SetupId.random());
    private static final BulkTarget SECTION_B = BulkTarget.of(SectionId.random());
    private static final BulkTarget SETUP_3 = BulkTarget.of(SetupId.random());

    private static final List<BulkTarget> ORDER =
            List.of(SECTION_A, SETUP_1, SETUP_2, SECTION_B, SETUP_3);

    private BulkSelection selection;

    @BeforeEach
    void setUp() {
        selection = new BulkSelection();
    }

    @Test
    void aPlainClickSelectsOne() {
        selection.click(SETUP_1, false, ORDER);
        assertTrue(selection.isSelected(SETUP_1));
        assertEquals(1, selection.size());
    }

    @Test
    void clickingTwiceDeselects() {
        selection.click(SETUP_1, false, ORDER);
        selection.click(SETUP_1, false, ORDER);
        assertFalse(selection.isSelected(SETUP_1));
        assertTrue(selection.isEmpty());
    }

    @Test
    void shiftClickSelectsTheRangeFromTheLastClick() {
        selection.click(SETUP_1, false, ORDER);
        selection.click(SECTION_B, true, ORDER);

        assertTrue(selection.isSelected(SETUP_1));
        assertTrue(selection.isSelected(SETUP_2));
        assertTrue(selection.isSelected(SECTION_B));
        assertFalse(selection.isSelected(SECTION_A));
        assertFalse(selection.isSelected(SETUP_3));
    }

    @Test
    void shiftClickWorksBackwards() {
        selection.click(SETUP_3, false, ORDER);
        selection.click(SETUP_2, true, ORDER);

        assertTrue(selection.isSelected(SETUP_2));
        assertTrue(selection.isSelected(SECTION_B));
        assertTrue(selection.isSelected(SETUP_3));
        assertEquals(3, selection.size());
    }

    @Test
    void shiftClickSpansSectionsAndSetupsAlike() {
        selection.click(SECTION_A, false, ORDER);
        selection.click(SETUP_3, true, ORDER);
        assertEquals(ORDER.size(), selection.size());
    }

    @Test
    void shiftClickWithoutAnAnchorBehavesLikeAPlainClick() {
        selection.click(SETUP_2, true, ORDER);
        assertEquals(1, selection.size());
        assertTrue(selection.isSelected(SETUP_2));
    }

    @Test
    void theAnchorStaysPutSoARangeCanBeRedrawn() {
        selection.click(SETUP_1, false, ORDER);
        selection.click(SETUP_3, true, ORDER);
        assertEquals(4, selection.size());

        selection.click(SETUP_2, true, ORDER);
        assertTrue(selection.isSelected(SETUP_1));
        assertTrue(selection.isSelected(SETUP_2));
    }

    @Test
    void separatesSectionsFromSetups() {
        selection.click(SECTION_A, false, ORDER);
        selection.click(SETUP_2, false, ORDER);

        assertEquals(Set.of(((BulkTarget.Section) SECTION_A).id()), selection.sections());
        assertEquals(Set.of(((BulkTarget.Setup) SETUP_2).id()), selection.setups());
    }

    @Test
    void retainingDropsTargetsThatNoLongerExist() {
        selection.click(SETUP_1, false, ORDER);
        selection.click(SETUP_2, false, ORDER);

        selection.retainAll(List.of(SETUP_1));

        assertTrue(selection.isSelected(SETUP_1));
        assertFalse(selection.isSelected(SETUP_2));
        assertEquals(1, selection.size());
    }

    @Test
    void clearingForgetsTheAnchorToo() {
        selection.click(SETUP_1, false, ORDER);
        selection.clear();
        selection.click(SETUP_3, true, ORDER);
        assertEquals(1, selection.size());
    }
}
