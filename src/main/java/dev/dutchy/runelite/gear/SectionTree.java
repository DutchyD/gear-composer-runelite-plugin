package dev.dutchy.runelite.gear;

import java.util.*;
import java.util.stream.Collectors;

/** The one-level nesting rules over a flat section list: children sit right after their parent, in list order. */
public final class SectionTree {

    private SectionTree() {
    }

    /** Puts every child straight after its parent and lifts children whose parent is missing or is itself a child. */
    public static List<GearSection> normalised(List<GearSection> sections) {
        Objects.requireNonNull(sections, "sections");
        Set<SectionId> topLevelIds = sections.stream()
                .filter(section -> !section.isChild())
                .map(GearSection::id)
                .collect(Collectors.toSet());
        Map<SectionId, List<GearSection>> families = new LinkedHashMap<>();
        for (GearSection section : sections) {
            boolean lifted = section.isChild() && !topLevelIds.contains(section.parent().orElseThrow());
            GearSection placed = lifted ? section.withoutParent() : section;
            SectionId key = placed.parent().orElse(placed.id());
            families.computeIfAbsent(key, id -> new ArrayList<>()).add(placed);
        }
        List<GearSection> ordered = new ArrayList<>();
        for (GearSection section : sections) {
            if (section.isChild() && topLevelIds.contains(section.parent().orElseThrow())) {
                continue;
            }
            List<GearSection> family = families.remove(section.id());
            if (family == null) {
                continue;
            }
            family.stream().filter(member -> !member.isChild()).forEach(ordered::add);
            family.stream().filter(GearSection::isChild).forEach(ordered::add);
        }
        return List.copyOf(ordered);
    }

    public static List<GearSection> topLevel(List<GearSection> sections) {
        return sections.stream().filter(section -> !section.isChild()).collect(Collectors.toList());
    }

    public static List<GearSection> childrenOf(List<GearSection> sections, SectionId parentId) {
        Objects.requireNonNull(parentId, "parentId");
        return sections.stream().filter(section -> section.isChildOf(parentId)).collect(Collectors.toList());
    }

    public static boolean hasChildren(List<GearSection> sections, SectionId parentId) {
        return !childrenOf(sections, parentId).isEmpty();
    }

    /** A top-level section with its children, or a child on its own. */
    public static List<GearSection> familyOf(List<GearSection> sections, SectionId id) {
        Objects.requireNonNull(id, "id");
        List<GearSection> family = new ArrayList<>();
        for (GearSection section : sections) {
            if (section.id().equals(id) || section.isChildOf(id)) {
                family.add(section);
            }
        }
        return family;
    }

    /** The sections that {@code id} can be reordered among: its siblings, or every top-level section. */
    public static List<GearSection> peersOf(List<GearSection> sections, SectionId id) {
        GearSection subject = find(sections, id);
        return subject.parent()
                .map(parent -> childrenOf(sections, parent))
                .orElseGet(() -> topLevel(sections));
    }

    static GearSection find(List<GearSection> sections, SectionId id) {
        Objects.requireNonNull(id, "id");
        return sections.stream()
                .filter(section -> section.id().equals(id))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown section " + id));
    }
}
