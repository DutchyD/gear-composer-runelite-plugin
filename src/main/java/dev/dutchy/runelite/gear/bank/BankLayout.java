package dev.dutchy.runelite.gear.bank;

import dev.dutchy.runelite.gear.layout.BankPlacement;
import dev.dutchy.runelite.gear.layout.BankSide;
import dev.dutchy.runelite.gear.layout.LayoutLabel;
import lombok.Value;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/** What a setup asks the bank to show. Placement coordinates are per side, not absolute bank slots. */
@Value
@Accessors(fluent = true)
public class BankLayout {
    List<BankPlacement> placements;
    List<LayoutLabel> labels;
    Set<Integer> headerRows;
    VariantTabs variants;

    public static final BankLayout EMPTY = new BankLayout(List.of());

    public BankLayout(List<BankPlacement> placements) {
        this(placements, List.of(), Set.of());
    }

    public BankLayout(List<BankPlacement> placements, List<LayoutLabel> labels, Set<Integer> headerRows) {
        this(placements, labels, headerRows, VariantTabs.NONE);
    }

    public BankLayout(List<BankPlacement> placements, List<LayoutLabel> labels, Set<Integer> headerRows, VariantTabs variants) {
        this.placements = List.copyOf(Objects.requireNonNull(placements, "placements"));
        this.labels = List.copyOf(Objects.requireNonNull(labels, "labels"));
        this.headerRows = Set.copyOf(Objects.requireNonNull(headerRows, "headerRows"));
        this.variants = Objects.requireNonNull(variants, "variants");
    }

    /** The same layout with the setup's variant tabs drawn above it. */
    public BankLayout withVariants(VariantTabs tabs) {
        return new BankLayout(placements, labels, headerRows, tabs);
    }

    /** Rows start below the variant tabs when there are any. */
    public BankRows rows() {
        return new BankRows(headerRows, variants.height());
    }

    public List<BankPlacement> side(BankSide side) {
        Objects.requireNonNull(side, "side");
        return placements.stream().filter(placement -> placement.side() == side).collect(Collectors.toList());
    }

    public boolean isEmpty() {
        return placements.isEmpty();
    }

    public int size() {
        return placements.size();
    }
}
