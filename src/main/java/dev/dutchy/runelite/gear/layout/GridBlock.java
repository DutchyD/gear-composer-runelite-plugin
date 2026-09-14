package dev.dutchy.runelite.gear.layout;

import dev.dutchy.runelite.gear.content.GridKind;
import dev.dutchy.runelite.gear.content.ItemGrid;
import dev.dutchy.runelite.gear.content.SlotRef;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

/** A four by seven item grid with its dividers. */
public final class GridBlock extends LayoutBlock {

    private final GridKind kind;
    private final ItemGrid grid;
    private final UnaryOperator<SlotRef> refs;

    GridBlock(String title, int band, BankSide side, int firstRow, Integer labelRow, GridKind kind, ItemGrid grid, UnaryOperator<SlotRef> refs) {
        super(title, band, side, firstRow, labelRow);
        this.kind = Objects.requireNonNull(kind, "kind");
        this.grid = Objects.requireNonNull(grid, "grid");
        this.refs = Objects.requireNonNull(refs, "refs");
    }

    public GridKind kind() {
        return kind;
    }

    public ItemGrid grid() {
        return grid;
    }

    public SlotRef ref(int index) {
        return refs.apply(SlotRef.of(kind, index));
    }

    @Override
    public int rowsTall() {
        return ItemGrid.ROWS;
    }

    @Override
    public List<Integer> headerRows() {
        return grid.rowsWithDividers().stream().map(row -> firstRow() + row).collect(Collectors.toList());
    }

    @Override
    public int columnsWide() {
        return ItemGrid.COLUMNS;
    }

    @Override
    public List<Slot> slots() {
        List<Slot> slots = new ArrayList<>();
        for (int index = 0; index < ItemGrid.SIZE; index++) {
            slots.add(new Slot(ref(index), grid.slot(index).orElse(null)));
        }
        return List.copyOf(slots);
    }

    @Override
    public List<BankPlacement> placements() {
        List<BankPlacement> placements = new ArrayList<>();
        for (int index = 0; index < ItemGrid.SIZE; index++) {
            int slotIndex = index;
            grid.slot(index).ifPresent(item -> placements.add(new BankPlacement(side(), ItemGrid.column(slotIndex),
                    firstRow() + ItemGrid.row(slotIndex), item)));
        }
        return List.copyOf(placements);
    }

    @Override
    List<LayoutLabel> labels() {
        List<LayoutLabel> labels = new ArrayList<>(super.labels());
        grid.dividers().forEach(divider -> labels.add(LayoutLabel.header(side(), firstRow() + divider.row(),
                divider.fromColumn(), divider.toColumn(), divider.label(), divider.align(), divider.underlined())));
        return List.copyOf(labels);
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.grid(this);
    }
}
