package dev.dutchy.runelite.gear.ui;

import dev.dutchy.runelite.gear.*;

import javax.inject.Inject;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/** Everything the sections themselves can be asked to do, and which of them are folded shut. */
public final class SectionCommands {

    public static final String NEW_SECTION_NAME = "New section";

    /** What the list does when a command needs something from it. */
    interface Listener {

        /** A section was just made, so its name wants typing straight away. */
        void renameStarted(SectionId sectionId);

        /** Which sections are folded changed, so the list is built again. */
        void foldingChanged();
    }

    private final GearSetupBook book;

    private Prompts prompts = SilentPrompts.INSTANCE;
    private Listener listener = new Listener() {
        @Override
        public void renameStarted(SectionId sectionId) {
        }

        @Override
        public void foldingChanged() {
        }
    };
    private Supplier<List<SectionId>> inView = List::of;

    private final Set<SectionId> folded = new HashSet<>();

    @Inject
    public SectionCommands(GearSetupBook book) {
        this.book = Objects.requireNonNull(book, "book");
    }

    /** Wired by the list, which owns the dialogs and knows the order the sections are drawn in. */
    void shownBy(Prompts newPrompts, Listener newListener, Supplier<List<SectionId>> sectionsInView) {
        this.prompts = Objects.requireNonNull(newPrompts, "newPrompts");
        this.listener = Objects.requireNonNull(newListener, "newListener");
        this.inView = Objects.requireNonNull(sectionsInView, "sectionsInView");
    }

    public void create() {
        made(book.addSection(NEW_SECTION_NAME).id());
    }

    public void insertAbove(SectionId sectionId) {
        made(book.insertSectionBefore(sectionId, NEW_SECTION_NAME).id());
    }

    public void insertBelow(SectionId sectionId) {
        made(book.insertSectionAfter(sectionId, NEW_SECTION_NAME).id());
    }

    public void addSub(SectionId parentId) {
        made(book.addSubSection(parentId, NEW_SECTION_NAME).id());
    }

    /** The book has already fired its change, so the list opens the field on the next build. */
    private void made(SectionId sectionId) {
        prompts.announce("Added a section");
        listener.renameStarted(sectionId);
    }

    public void rename(SectionId sectionId, String newName) {
        if (GearSection.isValidName(newName)) {
            book.renameSection(sectionId, newName);
            prompts.announce("Renamed to " + newName.strip());
        }
    }

    /** Says what goes with it, because a section takes its setups and hands its children up a level. */
    public void delete(SectionId sectionId) {
        Optional<GearSection> section = book.section(sectionId);
        if (section.isEmpty()) {
            return;
        }
        if (prompts.confirm(warningFor(section.get()), "Delete section")) {
            book.removeSection(sectionId);
        }
    }

    private String warningFor(GearSection section) {
        String warning = section.isEmpty()
                ? "Delete \"" + section.name() + "\"?"
                : "Delete \"" + section.name() + "\" and its " + section.size() + " setup(s)?";
        int children = SectionTree.childrenOf(book.sections(), section.id()).size();
        return children > 0 ? warning + "\nIts " + children + " sub-section(s) move to the top level." : warning;
    }

    public void sort(SectionId sectionId, SetupOrder order) {
        book.sortSection(sectionId, order.comparator());
        prompts.announce(order.displayName().replace("Sort by", "Sorted by"));
    }

    public void moveUp(SectionId sectionId) {
        book.moveSectionUp(sectionId);
    }

    public void moveDown(SectionId sectionId) {
        book.moveSectionDown(sectionId);
    }

    public void nest(SectionId sectionId, SectionId parentId) {
        Optional<GearSection> section = book.section(sectionId);
        Optional<GearSection> parent = book.section(parentId);
        if (section.isEmpty() || parent.isEmpty()) {
            return;
        }
        book.nestSection(sectionId, parentId);
        folded.remove(parentId);
        prompts.announce("Moved " + section.get().name() + " under " + parent.get().name());
    }

    public void unnest(SectionId sectionId) {
        Optional<GearSection> section = book.section(sectionId);
        if (section.isEmpty()) {
            return;
        }
        book.unnestSection(sectionId);
        prompts.announce("Moved " + section.get().name() + " to the top level");
    }

    /**
     * The caret counts every header drawn, while the book reorders among peers, so only the peers
     * above the caret decide where the section lands.
     */
    public void dropped(SectionId sectionId, int caret) {
        Optional<GearSection> dragged = book.section(sectionId);
        if (dragged.isEmpty()) {
            return;
        }
        List<SectionId> drawn = inView.get();
        int peersBefore = 0;
        for (int i = 0; i < Math.min(caret, drawn.size()); i++) {
            SectionId id = drawn.get(i);
            if (!id.equals(sectionId) && book.section(id).filter(other -> other.parent().equals(dragged.get().parent())).isPresent()) {
                peersBefore++;
            }
        }
        book.moveSection(sectionId, peersBefore);
    }

    public boolean isFolded(SectionId sectionId) {
        return folded.contains(sectionId);
    }

    public void toggleFolded(SectionId sectionId) {
        if (!folded.remove(sectionId)) {
            folded.add(sectionId);
        }
        listener.foldingChanged();
    }

    /** Folds everything but the one section and whatever it sits under. */
    public void foldOthers(SectionId sectionId) {
        folded.clear();
        Optional<SectionId> parent = book.section(sectionId).flatMap(GearSection::parent);
        book.sections().stream().map(GearSection::id)
                .filter(id -> !id.equals(sectionId) && parent.filter(id::equals).isEmpty())
                .forEach(folded::add);
        listener.foldingChanged();
    }

    /** Opens one section without redrawing, for a drag hovering over it. */
    public void unfold(SectionId sectionId) {
        folded.remove(sectionId);
    }

    /** Drops folds for sections that are gone. */
    void dropMissingFolds() {
        folded.retainAll(book.sections().stream().map(GearSection::id).collect(Collectors.toSet()));
    }
}
