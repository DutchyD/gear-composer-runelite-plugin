package dev.dutchy.runelite.gear;

import javax.inject.Inject;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Objects;

/** Snapshots of the book before each change, so any edit can be taken back. Used from the EDT. */
public final class UndoHistory implements GearSetupBookListener {

    public static final int DEFAULT_CAPACITY = 50;

    private final GearSetupBook book;
    private final int capacity;
    private final Deque<List<GearSection>> undo = new ArrayDeque<>();
    private final Deque<List<GearSection>> redo = new ArrayDeque<>();

    private List<GearSection> current;
    private boolean applying;

    @Inject
    public UndoHistory(GearSetupBook book) {
        this(book, DEFAULT_CAPACITY);
    }

    public UndoHistory(GearSetupBook book, int capacity) {
        this.book = Objects.requireNonNull(book, "book");
        if (capacity < 1) {
            throw new IllegalArgumentException("Capacity must be positive, got " + capacity);
        }
        this.capacity = capacity;
        this.current = book.sections();
        book.addChangeListener(this);
    }

    public boolean canUndo() {
        return !undo.isEmpty();
    }

    public boolean canRedo() {
        return !redo.isEmpty();
    }

    public boolean undo() {
        if (undo.isEmpty()) {
            return false;
        }
        redo.push(current);
        apply(undo.pop());
        return true;
    }

    public boolean redo() {
        if (redo.isEmpty()) {
            return false;
        }
        undo.push(current);
        apply(redo.pop());
        return true;
    }

    /** Forgets everything, for example after loading a different book. */
    public void reset() {
        undo.clear();
        redo.clear();
        current = book.sections();
    }

    @Override
    public void onBookChanged(GearSetupBook changed) {
        if (applying || changed != book) {
            return;
        }
        List<GearSection> next = book.sections();
        if (next.equals(current)) {
            return;
        }
        undo.push(current);
        while (undo.size() > capacity) {
            undo.removeLast();
        }
        redo.clear();
        current = next;
    }

    private void apply(List<GearSection> sections) {
        applying = true;
        try {
            current = sections;
            book.replaceSections(sections);
        } finally {
            applying = false;
        }
    }
}
