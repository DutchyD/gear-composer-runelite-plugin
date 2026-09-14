package dev.dutchy.runelite.gear.items;

import dev.dutchy.runelite.libs.ui.item.ItemId;

import java.util.*;

/** A hand-written family table, for tests and for supplementing the game data. */
public final class StaticItemVariants implements ItemVariants {

    private final Map<ItemId, List<ItemId>> families = new HashMap<>();

    public static StaticItemVariants of(List<List<ItemId>> families) {
        StaticItemVariants variants = new StaticItemVariants();
        families.forEach(variants::add);
        return variants;
    }

    public StaticItemVariants family(int... ids) {
        List<ItemId> family = new ArrayList<>();
        for (int id : ids) {
            family.add(ItemId.of(id));
        }
        add(family);
        return this;
    }

    private void add(List<ItemId> family) {
        Objects.requireNonNull(family, "family");
        List<ItemId> copy = List.copyOf(family);
        copy.forEach(member -> families.put(member, copy));
    }

    @Override
    public Collection<ItemId> familyOf(ItemId item) {
        Objects.requireNonNull(item, "item");
        List<ItemId> family = families.get(item);
        if (family == null) {
            return List.of(item);
        }
        List<ItemId> ordered = new ArrayList<>();
        ordered.add(item);
        family.stream().filter(member -> !member.equals(item)).forEach(ordered::add);
        return List.copyOf(ordered);
    }
}
