package dev.dutchy.runelite.gear.persistence;

import dev.dutchy.runelite.gear.*;

import java.util.*;

/** Appends imported sections to the current book, giving fresh ids to anything that already exists. */
public final class BookMerger {

    private BookMerger() {
    }

    public static List<GearSection> merge(List<GearSection> current, List<GearSection> imported) {
        Objects.requireNonNull(current, "current");
        Objects.requireNonNull(imported, "imported");
        Set<SectionId> sectionIds = new HashSet<>();
        Set<SetupId> setupIds = new HashSet<>();
        for (GearSection section : current) {
            sectionIds.add(section.id());
            section.setups().forEach(setup -> setupIds.add(setup.id()));
        }
        Map<SectionId, SectionId> renamed = new HashMap<>();
        for (GearSection section : imported) {
            SectionId id = sectionIds.add(section.id()) ? section.id() : SectionId.random();
            sectionIds.add(id);
            renamed.put(section.id(), id);
        }
        List<GearSection> merged = new ArrayList<>(current);
        for (GearSection section : imported) {
            List<GearSetup> setups = new ArrayList<>();
            for (GearSetup setup : section.setups()) {
                GearSetup added = setupIds.add(setup.id()) ? setup : setup.copyNamed(setup.name());
                setupIds.add(added.id());
                setups.add(added);
            }
            SectionId parent = section.parent().map(renamed::get).orElse(null);
            merged.add(new GearSection(renamed.get(section.id()), section.name(), setups, parent));
        }
        return SectionTree.normalised(merged);
    }
}
