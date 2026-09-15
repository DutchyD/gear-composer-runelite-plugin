package dev.dutchy.runelite.gear.layout;

import lombok.Value;

import java.util.*;
import java.util.stream.Collectors;

/** A horizontal strip of the layout: the blocks that sit side by side, and the rows they take together. */
@Value
public class Band {
    int index;
    List<LayoutBlock> blocks;

    public Band(int index, List<LayoutBlock> blocks) {
        this.index = index;
        this.blocks = List.copyOf(Objects.requireNonNull(blocks, "blocks"));
        if (this.blocks.isEmpty()) {
            throw new IllegalArgumentException("A band holds at least one block");
        }
    }

    public int rowsTall() {
        return blocks.stream().mapToInt(LayoutBlock::rowsTall).max().orElse(0);
    }

    /** Header rows of the band, relative to its first row. */
    public SortedSet<Integer> headerRows() {
        return blocks.stream().flatMap(block -> block.headerRows().stream()).map(row -> row - firstRow())
                .collect(Collectors.toCollection(TreeSet::new));
    }

    public Optional<Integer> labelRow() {
        return blocks.get(0).labelRow();
    }

    public int firstRow() {
        return blocks.get(0).firstRow();
    }

    public Optional<LayoutBlock> on(BankSide side) {
        return blocks.stream().filter(block -> block.side() == side).findFirst();
    }
}
