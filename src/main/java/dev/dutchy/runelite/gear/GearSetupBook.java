package dev.dutchy.runelite.gear;

import dev.dutchy.runelite.gear.content.SetupContent;
import dev.dutchy.runelite.gear.content.VariantId;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

/**
 * Always holds at least one section. Written on the dispatch thread, which every mutator asserts;
 * reads are served from an immutable snapshot and are safe from any thread.
 */
public final class GearSetupBook {

    public static final String DEFAULT_SECTION_NAME = "Setups";

    private final List<GearSection> sections = new ArrayList<>();
    private final List<GearSetupBookListener> listeners = new CopyOnWriteArrayList<>();
    private final Dispatch dispatch;

    private volatile List<GearSection> published;

    public GearSetupBook() {
        this(DEFAULT_SECTION_NAME, Dispatch.inline());
    }

    public GearSetupBook(String firstSectionName) {
        this(firstSectionName, Dispatch.inline());
    }

    public GearSetupBook(Dispatch dispatch) {
        this(DEFAULT_SECTION_NAME, dispatch);
    }

    public GearSetupBook(String firstSectionName, Dispatch dispatch) {
        this.dispatch = Objects.requireNonNull(dispatch, "dispatch");
        sections.add(GearSection.named(firstSectionName));
        published = List.copyOf(sections);
    }

    public List<GearSection> sections() {
        return published;
    }

    /** Swaps in a whole new set of sections, for example one read from storage. */
    public void replaceSections(List<GearSection> newSections) {
        requireDispatch();
        Objects.requireNonNull(newSections, "newSections");
        if (newSections.isEmpty()) {
            throw new IllegalArgumentException("A book needs at least one section");
        }
        sections.clear();
        sections.addAll(SectionTree.normalised(newSections));
        fireChanged();
    }

    public int sectionCount() {
        return published.size();
    }

    public int setupCount() {
        return published.stream().mapToInt(GearSection::size).sum();
    }

    public boolean isEmpty() {
        return setupCount() == 0;
    }

    public Optional<GearSection> section(SectionId id) {
        Objects.requireNonNull(id, "id");
        return published.stream().filter(section -> section.id().equals(id)).findFirst();
    }

    public Optional<GearSetup> setup(SetupId id) {
        Objects.requireNonNull(id, "id");
        return published.stream()
                .flatMap(section -> section.setups().stream())
                .filter(setup -> setup.id().equals(id))
                .findFirst();
    }

    public Optional<GearSection> sectionOf(SetupId id) {
        Objects.requireNonNull(id, "id");
        return published.stream()
                .filter(section -> section.setups().stream().anyMatch(setup -> setup.id().equals(id)))
                .findFirst();
    }

    public GearSection addSection(String name) {
        requireDispatch();
        GearSection section = GearSection.named(name);
        sections.add(section);
        fireChanged();
        return section;
    }

    public void renameSection(SectionId id, String newName) {
        requireDispatch();
        int index = indexOfSection(id);
        sections.set(index, sections.get(index).withName(newName));
        fireChanged();
    }

    /** Also removes the setups the section holds; removing a parent lifts its children to the top level in its place. */
    public void removeSection(SectionId id) {
        requireDispatch();
        int index = indexOfSection(id);
        if (sections.size() == 1) {
            throw new IllegalStateException("Cannot remove the last section");
        }
        sections.remove(index);
        for (int i = 0; i < sections.size(); i++) {
            if (sections.get(i).isChildOf(id)) {
                sections.set(i, sections.get(i).withoutParent());
            }
        }
        fireChanged();
    }

    /** A new empty section one level under {@code parentId}, placed after its existing children. */
    public GearSection addSubSection(SectionId parentId, String name) {
        requireDispatch();
        GearSection parent = requireTopLevel(parentId);
        GearSection child = new GearSection(SectionId.random(), name, List.of(), parent.id());
        sections.add(endOfFamily(parent.id()), child);
        fireChanged();
        return child;
    }

    /** A new empty peer of {@code anchorId}: a sibling for a child, a top-level section otherwise. */
    public GearSection insertSectionBefore(SectionId anchorId, String name) {
        requireDispatch();
        GearSection anchor = SectionTree.find(sections, anchorId);
        GearSection section = new GearSection(SectionId.random(), name, List.of(), anchor.parent().orElse(null));
        sections.add(indexOfSection(anchorId), section);
        fireChanged();
        return section;
    }

    public GearSection insertSectionAfter(SectionId anchorId, String name) {
        requireDispatch();
        GearSection anchor = SectionTree.find(sections, anchorId);
        GearSection section = new GearSection(SectionId.random(), name, List.of(), anchor.parent().orElse(null));
        sections.add(anchor.isChild() ? indexOfSection(anchorId) + 1 : endOfFamily(anchorId), section);
        fireChanged();
        return section;
    }

    /** Moves a childless top-level section under another top-level section, after its existing children. */
    public void nestSection(SectionId id, SectionId parentId) {
        requireDispatch();
        GearSection subject = SectionTree.find(sections, id);
        GearSection parent = requireTopLevel(parentId);
        if (subject.id().equals(parent.id())) {
            throw new IllegalArgumentException("A section cannot be nested under itself");
        }
        if (SectionTree.hasChildren(sections, id)) {
            throw new IllegalArgumentException("A section with sub-sections cannot be nested");
        }
        sections.remove(indexOfSection(id));
        sections.add(endOfFamily(parent.id()), subject.withParent(parent.id()));
        fireChanged();
    }

    /** Lifts a child out to the top level, right after the family it came from. */
    public void unnestSection(SectionId id) {
        requireDispatch();
        GearSection subject = SectionTree.find(sections, id);
        if (!subject.isChild()) {
            return;
        }
        SectionId parent = subject.parent().orElseThrow();
        sections.remove(indexOfSection(id));
        sections.add(endOfFamily(parent), subject.withoutParent());
        fireChanged();
    }

    private GearSection requireTopLevel(SectionId id) {
        GearSection section = SectionTree.find(sections, id);
        if (section.isChild()) {
            throw new IllegalArgumentException("Sections nest one level deep only");
        }
        return section;
    }

    /** The index just past a top-level section and its children. */
    private int endOfFamily(SectionId topLevelId) {
        int end = indexOfSection(topLevelId) + 1;
        while (end < sections.size() && sections.get(end).isChildOf(topLevelId)) {
            end++;
        }
        return end;
    }

    public GearSetup addSetup(SectionId sectionId, String name) {
        return addSetup(sectionId, GearSetup.named(name));
    }

    /** Adds a fully formed setup; its id must be new to the book. */
    public GearSetup addSetup(SectionId sectionId, GearSetup setup) {
        requireDispatch();
        Objects.requireNonNull(setup, "setup");
        if (setup(setup.id()).isPresent()) {
            throw new IllegalArgumentException("A setup with id " + setup.id() + " already exists");
        }
        int index = indexOfSection(sectionId);
        GearSection section = sections.get(index);
        List<GearSetup> updated = new ArrayList<>(section.setups());
        updated.add(setup);
        sections.set(index, section.withSetups(updated));
        fireChanged();
        return setup;
    }

    public GearSetup addSetup(String name) {
        requireDispatch();
        return addSetup(sections.get(0).id(), name);
    }

    /** Applies a change to one setup; nothing fires when the result is identical. Returns the setup afterwards. */
    public GearSetup change(SetupId id, UnaryOperator<GearSetup> change) {
        requireDispatch();
        Objects.requireNonNull(change, "change");
        GearSection section = sectionOf(id).orElseThrow(() -> unknownSetup(id));
        int sectionIndex = indexOfSection(section.id());
        List<GearSetup> updated = new ArrayList<>(section.setups());
        int setupIndex = indexOfSetup(updated, id);
        GearSetup before = updated.get(setupIndex);
        GearSetup after = Objects.requireNonNull(change.apply(before), "changed setup");
        if (!after.id().equals(id)) {
            throw new IllegalArgumentException("A change must keep the setup id");
        }
        if (after.equals(before)) {
            return before;
        }
        updated.set(setupIndex, after);
        sections.set(sectionIndex, section.withSetups(updated));
        fireChanged();
        return after;
    }

    public void setOwner(SetupId id, Owner owner) {
        Objects.requireNonNull(owner, "owner");
        change(id, setup -> setup.withOwner(owner));
    }

    public void changeMeta(SetupId id, UnaryOperator<SetupMeta> metaChange) {
        Objects.requireNonNull(metaChange, "metaChange");
        change(id, setup -> setup.withMeta(metaChange));
    }

    /** Inserts a copy right after the original, named so it does not clash within the section. */
    public GearSetup duplicateSetup(SetupId id) {
        requireDispatch();
        GearSection section = sectionOf(id).orElseThrow(() -> unknownSetup(id));
        int sectionIndex = indexOfSection(section.id());
        List<GearSetup> updated = new ArrayList<>(section.setups());
        int setupIndex = indexOfSetup(updated, id);
        GearSetup original = updated.get(setupIndex);
        GearSetup copy = original.copyNamed(SetupNames.copyOf(original.name(),
                updated.stream().map(GearSetup::name).collect(Collectors.toList())));
        updated.add(setupIndex + 1, copy);
        sections.set(sectionIndex, section.withSetups(updated));
        fireChanged();
        return copy;
    }

    public GearSection insertSection(int index, String name) {
        requireDispatch();
        GearSection section = GearSection.named(name);
        sections.add(Math.max(0, Math.min(sections.size(), index)), section);
        List<GearSection> normalised = SectionTree.normalised(sections);
        sections.clear();
        sections.addAll(normalised);
        fireChanged();
        return section;
    }

    /** Reorders one section's setups; the new order becomes the manual order. */
    public void sortSection(SectionId id, Comparator<GearSetup> order) {
        requireDispatch();
        Objects.requireNonNull(order, "order");
        int index = indexOfSection(id);
        GearSection section = sections.get(index);
        List<GearSetup> sorted = new ArrayList<>(section.setups());
        sorted.sort(order);
        if (sorted.equals(section.setups())) {
            return;
        }
        sections.set(index, section.withSetups(sorted));
        fireChanged();
    }

    /** Pinned setups in book order. */
    public List<GearSetup> pinnedSetups() {
        return published.stream()
                .flatMap(section -> section.setups().stream())
                .filter(GearSetup::isPinned)
                .collect(Collectors.toList());
    }


    /** Removes the sections and setups in one change. Recreates a default section if none remain. */
    public void deleteAll(Collection<SectionId> sectionIds, Collection<SetupId> setupIds) {
        requireDispatch();
        Objects.requireNonNull(sectionIds, "sectionIds");
        Objects.requireNonNull(setupIds, "setupIds");
        Set<SectionId> doomedSections = Set.copyOf(sectionIds);
        Set<SetupId> doomedSetups = Set.copyOf(setupIds);
        if (doomedSections.isEmpty() && doomedSetups.isEmpty()) {
            return;
        }
        List<GearSection> kept = new ArrayList<>();
        for (GearSection section : sections) {
            if (doomedSections.contains(section.id())) {
                continue;
            }
            List<GearSetup> survivors = section.setups().stream()
                    .filter(setup -> !doomedSetups.contains(setup.id()))
                    .collect(Collectors.toList());
            kept.add(survivors.size() == section.size() ? section : section.withSetups(survivors));
        }
        sections.clear();
        sections.addAll(SectionTree.normalised(kept));
        if (sections.isEmpty()) {
            sections.add(GearSection.named(DEFAULT_SECTION_NAME));
        }
        fireChanged();
    }

    /** Replaces the selected variant's contents. */
    public void updateSetupContent(SetupId id, SetupContent content) {
        updateVariantContent(id, setup(id).orElseThrow(() -> unknownSetup(id)).selected(), content);
    }

    /** Replaces one variant's contents. */
    public void updateVariantContent(SetupId id, VariantId variant, SetupContent content) {
        requireDispatch();
        Objects.requireNonNull(content, "content");
        GearSection section = sectionOf(id).orElseThrow(() -> unknownSetup(id));
        int sectionIndex = indexOfSection(section.id());
        List<GearSetup> updated = new ArrayList<>(section.setups());
        int setupIndex = indexOfSetup(updated, id);
        updated.set(setupIndex, updated.get(setupIndex).withVariantContent(variant, content));
        sections.set(sectionIndex, section.withSetups(updated));
        fireChanged();
    }

    public void removeSetup(SetupId id) {
        requireDispatch();
        GearSection section = sectionOf(id).orElseThrow(() -> unknownSetup(id));
        int sectionIndex = indexOfSection(section.id());
        List<GearSetup> updated = new ArrayList<>(section.setups());
        updated.remove(indexOfSetup(updated, id));
        sections.set(sectionIndex, section.withSetups(updated));
        fireChanged();
    }

    /** Reorders among peers: siblings for a child, top-level sections otherwise, each moving with its children. targetIndex applies after removal and is clamped. */
    public boolean moveSection(SectionId id, int targetIndex) {
        requireDispatch();
        GearSection moved = SectionTree.find(sections, id);
        List<GearSection> peers = new ArrayList<>(SectionTree.peersOf(sections, id));
        int from = indexOfSection(peers, id);
        peers.remove(from);
        int clamped = Math.max(0, Math.min(peers.size(), targetIndex));
        if (clamped == from) {
            return false;
        }
        peers.add(clamped, moved);
        applyPeerOrder(moved, peers);
        fireChanged();
        return true;
    }

    public boolean moveSectionUp(SectionId id) {
        requireDispatch();
        int index = indexOfSection(SectionTree.peersOf(sections, id), id);
        return index > 0 && moveSection(id, index - 1);
    }

    public boolean moveSectionDown(SectionId id) {
        requireDispatch();
        List<GearSection> peers = SectionTree.peersOf(sections, id);
        int index = indexOfSection(peers, id);
        return index < peers.size() - 1 && moveSection(id, index + 1);
    }

    private void applyPeerOrder(GearSection subject, List<GearSection> peers) {
        List<GearSection> result = new ArrayList<>();
        if (subject.isChild()) {
            SectionId parent = subject.parent().orElseThrow();
            for (GearSection section : sections) {
                if (section.isChildOf(parent)) {
                    continue;
                }
                result.add(section);
                if (section.id().equals(parent)) {
                    result.addAll(peers);
                }
            }
        } else {
            for (GearSection top : peers) {
                result.addAll(SectionTree.familyOf(sections, top.id()));
            }
        }
        sections.clear();
        sections.addAll(result);
    }

    private static int indexOfSection(List<GearSection> list, SectionId id) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).id().equals(id)) {
                return i;
            }
        }
        throw new IllegalArgumentException("Unknown section " + id);
    }

    public int sectionIndex(SectionId id) {
        Objects.requireNonNull(id, "id");
        return indexOfSection(published, id);
    }

    /** targetIndex applies after the setup is removed from its current position, and is clamped. */
    public boolean moveSetup(SetupId id, SectionId targetSectionId, int targetIndex) {
        requireDispatch();
        GearSection source = sectionOf(id).orElseThrow(() -> unknownSetup(id));
        int targetSectionIndex = indexOfSection(targetSectionId);
        int sourceSectionIndex = indexOfSection(source.id());

        List<GearSetup> sourceSetups = new ArrayList<>(source.setups());
        int sourceIndex = indexOfSetup(sourceSetups, id);
        GearSetup moved = sourceSetups.remove(sourceIndex);

        if (sourceSectionIndex == targetSectionIndex) {
            int clamped = Math.max(0, Math.min(sourceSetups.size(), targetIndex));
            if (clamped == sourceIndex) {
                return false;
            }
            sourceSetups.add(clamped, moved);
            sections.set(sourceSectionIndex, source.withSetups(sourceSetups));
        } else {
            GearSection target = sections.get(targetSectionIndex);
            List<GearSetup> targetSetups = new ArrayList<>(target.setups());
            targetSetups.add(Math.max(0, Math.min(targetSetups.size(), targetIndex)), moved);
            sections.set(sourceSectionIndex, source.withSetups(sourceSetups));
            sections.set(targetSectionIndex, target.withSetups(targetSetups));
        }
        fireChanged();
        return true;
    }

    public void addChangeListener(GearSetupBookListener listener) {
        listeners.add(Objects.requireNonNull(listener, "listener"));
    }

    public void removeChangeListener(GearSetupBookListener listener) {
        listeners.remove(listener);
    }

    private void fireChanged() {
        published = List.copyOf(sections);
        for (GearSetupBookListener listener : listeners) {
            listener.onBookChanged(this);
        }
    }

    private void requireDispatch() {
        if (!dispatch.isCurrent()) {
            throw new IllegalStateException("The book must be changed on the dispatch thread");
        }
    }

    private int indexOfSection(SectionId id) {
        Objects.requireNonNull(id, "id");
        for (int i = 0; i < sections.size(); i++) {
            if (sections.get(i).id().equals(id)) {
                return i;
            }
        }
        throw new IllegalArgumentException("Unknown section " + id);
    }

    private static int indexOfSetup(List<GearSetup> setups, SetupId id) {
        for (int i = 0; i < setups.size(); i++) {
            if (setups.get(i).id().equals(id)) {
                return i;
            }
        }
        throw unknownSetup(id);
    }

    private static IllegalArgumentException unknownSetup(SetupId id) {
        return new IllegalArgumentException("Unknown setup " + id);
    }
}
