package dev.dutchy.runelite.gear.layout;

import dev.dutchy.runelite.gear.content.*;

import java.util.*;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

/**
 * A setup's contents as blocks in bands, the one place that knows what each layout type looks like.
 * Everything that draws, plans, sums or compares a setup walks these blocks instead of asking the shape.
 */
public final class Layout {

    private final List<LayoutBlock> blocks;

    private Layout(List<LayoutBlock> blocks) {
        this.blocks = List.copyOf(blocks);
    }

    public static Layout of(SetupContent content) {
        Objects.requireNonNull(content, "content");
        if (content instanceof GearContent) {
            return ofGear((GearContent) content);
        }
        if (content instanceof BankContent) {
            return ofBank((BankContent) content);
        }
        return ofCustom((CustomContent) content);
    }

    public List<LayoutBlock> blocks() {
        return blocks;
    }

    public List<Band> bands() {
        Map<Integer, List<LayoutBlock>> grouped = new TreeMap<>();
        blocks.forEach(block -> grouped.computeIfAbsent(block.band(), b -> new ArrayList<>()).add(block));
        return grouped.entrySet().stream().map(entry -> new Band(entry.getKey(), entry.getValue())).collect(Collectors.toList());
    }

    /** Bank rows the whole layout takes. */
    public int rowsTall() {
        return blocks.stream().mapToInt(block -> block.firstRow() + block.rowsTall()).max().orElse(0);
    }

    public List<BankPlacement> placements() {
        return blocks.stream().flatMap(block -> block.placements().stream()).collect(Collectors.toList());
    }

    public List<LayoutLabel> labels() {
        return blocks.stream().flatMap(block -> block.labels().stream()).collect(Collectors.toList());
    }

    /** Bank rows that carry a header strip on either side; both sides grow together so they stay aligned. */
    public SortedSet<Integer> headerRows() {
        return blocks.stream().flatMap(block -> block.headerRows().stream()).collect(Collectors.toCollection(TreeSet::new));
    }

    /** Every slot of every block, in reading order. */
    public List<LayoutBlock.Slot> slots() {
        return blocks.stream().flatMap(block -> block.slots().stream()).collect(Collectors.toList());
    }

    public List<SetupItem> items() {
        return blocks.stream().flatMap(block -> block.items().stream()).collect(Collectors.toList());
    }

    public boolean hasLabelledBands() {
        return bands().stream().anyMatch(band -> band.labelRow().isPresent());
    }

    private static Layout ofGear(GearContent gear) {
        return new Layout(List.of(
                new EquipmentBlock("Equipment", 0, BankSide.LEFT, 0, null, gear.equipment(), UnaryOperator.identity(), true),
                new GridBlock("Inventory", 0, BankSide.RIGHT, 0, null, GridKind.INVENTORY, gear.inventory(), UnaryOperator.identity())));
    }

    private static Layout ofBank(BankContent bank) {
        return new Layout(List.of(
                new GridBlock("Left side", 0, BankSide.LEFT, 0, null, GridKind.LEFT, bank.left(), UnaryOperator.identity()),
                new GridBlock("Right side", 0, BankSide.RIGHT, 0, null, GridKind.RIGHT, bank.right(), UnaryOperator.identity())));
    }

    private static Layout ofCustom(CustomContent custom) {
        List<LayoutBlock> blocks = new ArrayList<>();
        int cursor = 0;
        for (int row = 0; row < custom.rows(); row++) {
            List<LayoutBlock> band = new ArrayList<>();
            for (int column = 0; column < CustomContent.COLUMNS; column++) {
                CellRef ref = CellRef.of(row, column);
                LayoutCell cell = custom.cell(ref);
                BankSide side = ref.isLeft() ? BankSide.LEFT : BankSide.RIGHT;
                UnaryOperator<SlotRef> refs = inner -> SlotRef.in(ref, inner);
                if (cell.kind() == CellKind.EQUIPMENT) {
                    band.add(new EquipmentBlock(cell.label(), row, side, cursor + 1, cursor, cell.equipment(), refs, false));
                } else if (cell.kind() == CellKind.INVENTORY) {
                    band.add(new GridBlock(cell.label(), row, side, cursor + 1, cursor, GridKind.INVENTORY, cell.inventory(), refs));
                }
            }
            if (band.isEmpty()) {
                continue;
            }
            blocks.addAll(band);
            cursor += 1 + band.stream().mapToInt(LayoutBlock::rowsTall).max().orElse(0);
        }
        return new Layout(blocks);
    }
}
