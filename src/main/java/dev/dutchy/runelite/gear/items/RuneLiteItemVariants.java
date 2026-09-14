package dev.dutchy.runelite.gear.items;

import dev.dutchy.runelite.libs.ui.item.ItemId;
import net.runelite.client.game.ItemVariationMapping;

import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/** Backed by RuneLite's item variation table. */
@Singleton
public final class RuneLiteItemVariants implements ItemVariants {

    @Override
    public Collection<ItemId> familyOf(ItemId item) {
        Objects.requireNonNull(item, "item");
        int base = ItemVariationMapping.map(item.value());
        List<ItemId> family = new ArrayList<>();
        family.add(item);
        for (int variation : ItemVariationMapping.getVariations(base)) {
            ItemId candidate = ItemId.of(variation);
            if (!family.contains(candidate)) {
                family.add(candidate);
            }
        }
        return List.copyOf(family);
    }
}
