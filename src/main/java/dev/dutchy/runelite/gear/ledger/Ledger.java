package dev.dutchy.runelite.gear.ledger;

import dev.dutchy.runelite.gear.content.*;
import dev.dutchy.runelite.gear.layout.EquipmentBlock;
import dev.dutchy.runelite.gear.layout.GridBlock;
import dev.dutchy.runelite.gear.layout.Layout;
import dev.dutchy.runelite.gear.layout.LayoutBlock;
import dev.dutchy.runelite.libs.ui.item.ItemId;
import lombok.Value;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/** Totals for a setup: coins, kilograms, and worn bonuses. Slots that take the bank amount count once and mark the value open-ended. */
@Value
@Accessors(fluent = true)
public class Ledger {
    long value;
    boolean openEnded;
    double weight;
    EquipmentStats stats;
    int unknownItems;
    int itemCount;

    public static final Ledger EMPTY = new Ledger(0, false, 0, EquipmentStats.ZERO, 0, 0);

    public static Ledger of(SetupContent content, ItemFactsSource facts) {
        Objects.requireNonNull(content, "content");
        Objects.requireNonNull(facts, "facts");
        Builder builder = new Builder(facts);
        for (LayoutBlock block : Layout.of(content).blocks()) {
            block.accept(new LayoutBlock.Visitor<Void>() {
                @Override
                public Void equipment(EquipmentBlock equipment) {
                    builder.wear(equipment.equipment(), equipment.countsWornBonuses());
                    return null;
                }

                @Override
                public Void grid(GridBlock grid) {
                    builder.carry(grid.grid());
                    return null;
                }
            });
        }
        return builder.build();
    }

    /** One equipment cell of a custom layout on its own, bonuses included. */
    public static Ledger ofCell(LayoutCell cell, ItemFactsSource facts) {
        return ofEquipment(Objects.requireNonNull(cell, "cell").equipment(), facts);
    }

    public static Ledger ofEquipment(Map<EquipmentSlot, SetupItem> equipment, ItemFactsSource facts) {
        Builder builder = new Builder(Objects.requireNonNull(facts, "facts"));
        builder.wear(equipment);
        return builder.build();
    }

    public boolean hasUnknownItems() {
        return unknownItems > 0;
    }

    private static final class Builder {
        private final ItemFactsSource facts;
        private long value;
        private boolean openEnded;
        private double weight;
        private EquipmentStats stats = EquipmentStats.ZERO;
        private int unknown;
        private int count;

        Builder(ItemFactsSource facts) {
            this.facts = facts;
        }

        void wear(Map<EquipmentSlot, SetupItem> equipment) {
            wear(equipment, true);
        }

        /** Bonuses are summed only when asked: several equipment sets in one layout do not add up. */
        void wear(Map<EquipmentSlot, SetupItem> equipment, boolean withBonuses) {
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                SetupItem item = equipment.get(slot);
                if (item == null) {
                    continue;
                }
                Optional<ItemFacts> known = add(item, true);
                if (withBonuses) {
                    known.flatMap(ItemFacts::equipment).ifPresent(bonus -> stats = stats.plus(bonus));
                }
            }
        }

        void carry(ItemGrid grid) {
            for (int index = 0; index < ItemGrid.SIZE; index++) {
                grid.slot(index).ifPresent(item -> add(item, false));
            }
        }

        /** A worn item is always exactly one; a carried slot without an amount takes whatever the bank holds. */
        private Optional<ItemFacts> add(SetupItem item, boolean worn) {
            count++;
            Optional<ItemFacts> known = facts.factsOf(item.id());
            if (known.isEmpty()) {
                unknown++;
                return known;
            }
            int quantity = worn ? 1 : item.quantity().orElse(1);
            if (!worn && !item.hasQuantity()) {
                openEnded = true;
            }
            value += (long) known.get().price() * quantity;
            weight += known.get().weight() * quantity;
            return known;
        }

        Ledger build() {
            return new Ledger(value, openEnded, weight, stats, unknown, count);
        }
    }

    /** Every item id the ledger would ask about, for warming a source. */
    public static List<ItemId> itemsOf(SetupContent content) {
        return SetupContentEditor.allItems(content).stream().map(SetupItem::id).distinct().collect(Collectors.toList());
    }
}
