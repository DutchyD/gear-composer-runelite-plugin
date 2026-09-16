package dev.dutchy.runelite.gear.bank;

import dev.dutchy.runelite.gear.content.SetupItem;
import dev.dutchy.runelite.gear.layout.BankPlacement;
import dev.dutchy.runelite.gear.layout.BankSide;
import dev.dutchy.runelite.libs.ui.item.ItemId;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Maps a two-sided layout onto the bank's eight-wide grid. The left side takes the first four
 * columns and the right side the last four.
 */
@Singleton
public final class BankGrid {

    public static final int ITEMS_PER_ROW = 8;
    public static final int COLUMNS_PER_SIDE = ITEMS_PER_ROW / 2;

    private final SlotResolver resolver;

    @Inject
    public BankGrid(SlotResolver resolver) {
        this.resolver = Objects.requireNonNull(resolver, "resolver");
    }

    /** Slot index to item, where a slot index is row times {@link #ITEMS_PER_ROW} plus column. */
    public Map<Integer, SetupItem> map(BankLayout layout) {
        Objects.requireNonNull(layout, "layout");
        Map<Integer, SetupItem> slots = new LinkedHashMap<>();
        for (BankPlacement placement : layout.placements()) {
            slots.put(slotIndex(placement), placement.item());
        }
        return Map.copyOf(slots);
    }

    /** Slots to draw in display order, marking the ones the bank cannot supply. */
    public List<BankSlotPlan> plan(BankLayout layout, BankContents contents) {
        return plan(layout, contents, Map.of());
    }

    /** The same, with the player's own choice of which family member to draw in a slot. */
    public List<BankSlotPlan> plan(BankLayout layout, BankContents contents, Map<Integer, ItemId> chosen) {
        Objects.requireNonNull(contents, "contents");
        Objects.requireNonNull(chosen, "chosen");
        return map(layout).entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> {
                    SlotSupply supply = resolver.supply(entry.getValue(), contents);
                    ItemId choice = chosen.get(entry.getKey());
                    return new BankSlotPlan(entry.getKey(), entry.getValue(), choice == null ? supply : supply.showing(choice));
                })
                .collect(Collectors.toList());
    }

    public int rowsNeeded(BankLayout layout) {
        Objects.requireNonNull(layout, "layout");
        return layout.placements().stream().mapToInt(BankPlacement::row).max().orElse(-1) + 1;
    }

    public static int slotIndex(BankPlacement placement) {
        Objects.requireNonNull(placement, "placement");
        if (placement.column() >= COLUMNS_PER_SIDE) {
            throw new IllegalArgumentException("A side is only " + COLUMNS_PER_SIDE
                    + " columns wide, got column " + placement.column());
        }
        return placement.row() * ITEMS_PER_ROW + absoluteColumn(placement);
    }

    private static int absoluteColumn(BankPlacement placement) {
        return placement.side() == BankSide.LEFT
                ? placement.column()
                : COLUMNS_PER_SIDE + placement.column();
    }
}
