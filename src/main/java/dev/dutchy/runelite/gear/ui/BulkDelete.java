package dev.dutchy.runelite.gear.ui;

import dev.dutchy.runelite.gear.GearSection;
import dev.dutchy.runelite.gear.GearSetupBook;
import dev.dutchy.runelite.gear.SetupId;
import dev.dutchy.runelite.gear.history.SetupHistory;
import lombok.Getter;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Picking several setups and sections and deleting them in one go. Owns whether the list is in bulk
 * mode and what is picked; a section takes its setups and their history with it.
 */
public final class BulkDelete implements BulkSelector {

    static final String PICK_HINT = "Pick what to delete. Shift click for a range.";

    /** What the list does when the mode or the selection changes. */
    interface Listener {

        /** The mode changed, so the list is built again. */
        void modeChanged();

        /** The selection changed, so the marks and the count are refreshed. */
        void selectionChanged();
    }

    private final GearSetupBook book;
    private final SetupHistory history;

    private Prompts prompts = SilentPrompts.INSTANCE;
    private Listener listener = new Listener() {
        @Override
        public void modeChanged() {
        }

        @Override
        public void selectionChanged() {
        }
    };
    private Supplier<List<BulkTarget>> inView = List::of;

    private final BulkSelection selection = new BulkSelection();

    /** Whether the list is picking things to delete. */
    @Getter
    private boolean on;

    @Inject
    public BulkDelete(GearSetupBook book, SetupHistory history) {
        this.book = Objects.requireNonNull(book, "book");
        this.history = Objects.requireNonNull(history, "history");
    }

    /** Wired by the list, which owns the dialogs and knows the order a shift click ranges over. */
    void shownBy(Prompts newPrompts, Listener newListener, Supplier<List<BulkTarget>> targetsInView) {
        this.prompts = Objects.requireNonNull(newPrompts, "newPrompts");
        this.listener = Objects.requireNonNull(newListener, "newListener");
        this.inView = Objects.requireNonNull(targetsInView, "targetsInView");
    }

    public void toggle() {
        set(!on);
    }

    public void set(boolean enabled) {
        on = enabled;
        selection.clear();
        if (enabled) {
            prompts.say(PICK_HINT);
        }
        listener.modeChanged();
    }

    public int count() {
        return selection.size();
    }

    String countText() {
        return selection.size() + " selected";
    }

    @Override
    public boolean isSelected(BulkTarget target) {
        return selection.isSelected(target);
    }

    @Override
    public void select(BulkTarget target, boolean shiftDown) {
        selection.click(target, shiftDown, inView.get());
        listener.selectionChanged();
    }

    /** Drops what no longer exists, so something already deleted cannot linger in the selection. */
    void dropMissing() {
        selection.retainAll(everything());
    }

    /** Asks first, then deletes everything picked. */
    public void deleteSelected() {
        int count = selection.size();
        if (count == 0 || !prompts.confirm("Delete " + count + " selected item(s)?", "Bulk delete")) {
            return;
        }
        List<SetupId> doomed = new ArrayList<>(selection.setups());
        book.sections().stream()
                .filter(section -> selection.sections().contains(section.id()))
                .forEach(section -> section.setups().forEach(setup -> doomed.add(setup.id())));
        book.deleteAll(selection.sections(), selection.setups());
        doomed.forEach(history::forget);
        set(false);
        prompts.announce("Deleted " + count + (count == 1 ? " item" : " items"));
    }

    private List<BulkTarget> everything() {
        List<BulkTarget> targets = new ArrayList<>();
        for (GearSection section : book.sections()) {
            targets.add(BulkTarget.of(section.id()));
            section.setups().forEach(setup -> targets.add(BulkTarget.of(setup.id())));
        }
        return targets;
    }
}
