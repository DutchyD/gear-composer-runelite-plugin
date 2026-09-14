package dev.dutchy.runelite.gear.ui;

import dev.dutchy.runelite.gear.content.GridKind;
import dev.dutchy.runelite.gear.content.SlotRef;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SlotSelectionTest {

    private static final List<SlotRef> ORDER = order();

    private static List<SlotRef> order() {
        List<SlotRef> refs = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            refs.add(SlotRef.of(GridKind.INVENTORY, i));
        }
        return refs;
    }

    private final SlotSelection selection = new SlotSelection();

    @Test
    void aPlainClickSelectsJustThatSlot() {
        selection.click(ORDER.get(1), false, false, ORDER);
        selection.click(ORDER.get(3), false, false, ORDER);
        assertEquals(List.of(ORDER.get(3)), selection.ordered(ORDER));
    }

    @Test
    void shiftClickSelectsTheRangeFromTheAnchor() {
        selection.click(ORDER.get(4), false, false, ORDER);
        selection.click(ORDER.get(1), true, false, ORDER);
        assertEquals(ORDER.subList(1, 5), selection.ordered(ORDER));
    }

    @Test
    void controlClickTogglesWithoutLosingTheRest() {
        selection.click(ORDER.get(0), false, false, ORDER);
        selection.click(ORDER.get(2), false, true, ORDER);
        selection.click(ORDER.get(0), false, true, ORDER);
        assertEquals(List.of(ORDER.get(2)), selection.ordered(ORDER));
        selection.clear();
        assertTrue(selection.isEmpty());
    }
}
