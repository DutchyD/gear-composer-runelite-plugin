package dev.dutchy.runelite.gear;

import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UndoHistoryTest {

    private final GearSetupBook book = new GearSetupBook();
    private final UndoHistory history = new UndoHistory(book, 3);
    private final SectionId section = book.sections().get(0).id();

    private List<String> names() {
        return book.sections().get(0).setups().stream().map(GearSetup::name).collect(Collectors.toList());
    }

    @Test
    void nothingToUndoAtFirst() {
        assertFalse(history.canUndo());
        assertFalse(history.undo());
    }

    @Test
    void undoRestoresTheStateBeforeTheLastChange() {
        book.addSetup(section, "Vorkath");
        book.addSetup(section, "Zulrah");

        assertTrue(history.undo());
        assertEquals(List.of("Vorkath"), names());
        assertTrue(history.undo());
        assertEquals(List.of(), names());
        assertFalse(history.canUndo());
    }

    @Test
    void redoReappliesAnUndoneChangeUntilANewChangeIsMade() {
        book.addSetup(section, "Vorkath");
        history.undo();
        assertTrue(history.canRedo());

        assertTrue(history.redo());
        assertEquals(List.of("Vorkath"), names());
        assertFalse(history.canRedo());

        history.undo();
        book.addSetup(section, "Zulrah");
        assertFalse(history.canRedo(), "a fresh change discards the redo branch");
        assertEquals(List.of("Zulrah"), names());
    }

    @Test
    void theOldestSnapshotsFallOffPastTheCapacity() {
        for (String name : List.of("A", "B", "C", "D", "E")) {
            book.addSetup(section, name);
        }
        int undone = 0;
        while (history.undo()) {
            undone++;
        }
        assertEquals(3, undone);
        assertEquals(List.of("A", "B"), names());
    }

    @Test
    void undoingIsNotRecordedAsAChangeOfItsOwn() {
        book.addSetup(section, "Vorkath");
        history.undo();
        assertFalse(history.canUndo());
        assertTrue(history.canRedo());
    }

    @Test
    void resetForgetsEverything() {
        book.addSetup(section, "Vorkath");
        history.reset();
        assertFalse(history.canUndo());
        book.addSetup(section, "Zulrah");
        assertTrue(history.undo());
        assertEquals(List.of("Vorkath"), names());
    }
}
