package dev.dutchy.runelite.gear.content;

import dev.dutchy.runelite.gear.layout.Layout;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.UnaryOperator;

/** Reads and writes a single slot of any setup content, whichever shape it has. */
public final class SetupContentEditor {

    private SetupContentEditor() {
    }

    public static Optional<SetupItem> itemAt(SetupContent content, SlotRef ref) {
        Objects.requireNonNull(content, "content");
        Objects.requireNonNull(ref, "ref");
        if (ref instanceof SlotRef.Cell) {
            SlotRef.Cell cell = (SlotRef.Cell) ref;
            return cellOf(content, cell.cell()).flatMap(found -> found.item(cell.inner()));
        }
        if (ref instanceof SlotRef.Equipment) {
            SlotRef.Equipment equipment = (SlotRef.Equipment) ref;
            return content instanceof GearContent ? ((GearContent) content).equipped(equipment.slot()) : Optional.empty();
        }
        SlotRef.Grid grid = (SlotRef.Grid) ref;
        return gridFor(content, grid.grid()).flatMap(target -> target.slot(grid.index()));
    }

    public static SetupContent withItem(SetupContent content, SlotRef ref, SetupItem item) {
        Objects.requireNonNull(content, "content");
        Objects.requireNonNull(ref, "ref");
        Objects.requireNonNull(item, "item");
        if (ref instanceof SlotRef.Cell) {
            SlotRef.Cell cell = (SlotRef.Cell) ref;
            return replaceCell(content, cell.cell(), found -> found.withItem(cell.inner(), item));
        }
        if (ref instanceof SlotRef.Equipment) {
            SlotRef.Equipment equipment = (SlotRef.Equipment) ref;
            return content instanceof GearContent ? ((GearContent) content).withEquipped(equipment.slot(), item) : content;
        }
        SlotRef.Grid grid = (SlotRef.Grid) ref;
        return replaceGrid(content, grid.grid(), target -> target.withSlot(grid.index(), item));
    }

    public static SetupContent cleared(SetupContent content, SlotRef ref) {
        Objects.requireNonNull(content, "content");
        Objects.requireNonNull(ref, "ref");
        if (ref instanceof SlotRef.Cell) {
            SlotRef.Cell cell = (SlotRef.Cell) ref;
            return replaceCell(content, cell.cell(), found -> found.withoutItem(cell.inner()));
        }
        if (ref instanceof SlotRef.Equipment) {
            SlotRef.Equipment equipment = (SlotRef.Equipment) ref;
            return content instanceof GearContent ? ((GearContent) content).withoutEquipped(equipment.slot()) : content;
        }
        SlotRef.Grid grid = (SlotRef.Grid) ref;
        return replaceGrid(content, grid.grid(), target -> target.clearSlot(grid.index()));
    }

    /** Moves an item to another slot, swapping with whatever is there; with {@code copy} the source stays. */
    public static SetupContent moved(SetupContent content, SlotRef from, SlotRef to, boolean copy) {
        Objects.requireNonNull(content, "content");
        Objects.requireNonNull(from, "from");
        Objects.requireNonNull(to, "to");
        Optional<SetupItem> moving = itemAt(content, from);
        if (moving.isEmpty() || from.equals(to) || !supports(content, to)) {
            return content;
        }
        Optional<SetupItem> displaced = itemAt(content, to);
        SetupContent result = withItem(content, to, moving.get());
        if (copy) {
            return result;
        }
        return displaced.map(item -> withItem(result, from, item)).orElseGet(() -> cleared(result, from));
    }

    /** Copies the item in {@code from} into every empty slot after it in the same grid. */
    public static SetupContent filledFrom(SetupContent content, SlotRef from) {
        return filled(content, from, from.gridIndex() + 1, ItemGrid.SIZE);
    }

    /** Copies the item in {@code from} into the empty slots of its row. */
    public static SetupContent filledRow(SetupContent content, SlotRef from) {
        int rowStart = ItemGrid.row(from.gridIndex()) * ItemGrid.COLUMNS;
        return filled(content, from, rowStart, rowStart + ItemGrid.COLUMNS);
    }

    private static SetupContent filled(SetupContent content, SlotRef from, int start, int end) {
        Objects.requireNonNull(content, "content");
        Objects.requireNonNull(from, "from");
        Optional<SetupItem> source = itemAt(content, from);
        if (source.isEmpty() || !from.inGrid()) {
            return content;
        }
        SetupContent result = content;
        for (int index = start; index < end; index++) {
            SlotRef target = from.gridSibling(index);
            if (index != from.gridIndex() && itemAt(result, target).isEmpty()) {
                result = withItem(result, target, source.get());
            }
        }
        return result;
    }

    /** Lifts the items in the given slots, in the given order, skipping empty ones. */
    public static SlotClipboard copied(SetupContent content, List<SlotRef> refs) {
        Objects.requireNonNull(content, "content");
        Objects.requireNonNull(refs, "refs");
        List<SlotClipboard.Entry> entries = new ArrayList<>();
        for (SlotRef ref : refs) {
            itemAt(content, ref).ifPresent(item -> entries.add(new SlotClipboard.Entry(ref, item)));
        }
        return new SlotClipboard(entries);
    }

    /**
     * Puts clipboard items down: slot-bound entries such as equipment go back to their own slot, grid
     * entries fill consecutive slots of the anchor's grid. Entries that do not fit are skipped.
     */
    public static SetupContent pasted(SetupContent content, SlotClipboard clipboard, SlotRef anchor) {
        Objects.requireNonNull(content, "content");
        Objects.requireNonNull(clipboard, "clipboard");
        Objects.requireNonNull(anchor, "anchor");
        SetupContent result = content;
        int next = anchor.inGrid() ? anchor.gridIndex() : ItemGrid.SIZE;
        for (SlotClipboard.Entry entry : clipboard.entries()) {
            SlotRef target;
            if (entry.from().inGrid()) {
                if (next >= ItemGrid.SIZE) {
                    continue;
                }
                target = anchor.gridSibling(next++);
            } else {
                target = entry.from();
            }
            if (supports(result, target)) {
                result = withItem(result, target, entry.item());
            }
        }
        return result;
    }

    /** The divider over a grid slot's column, if the slot is in a grid and one spans it. */
    public static Optional<Divider> dividerAt(SetupContent content, SlotRef ref) {
        return gridOf(content, ref).flatMap(grid -> grid.dividerAt(ItemGrid.row(ref.gridIndex()), ItemGrid.column(ref.gridIndex())));
    }

    /** Every divider above a grid slot's row. */
    public static List<Divider> dividersAbove(SetupContent content, SlotRef ref) {
        return gridOf(content, ref).map(grid -> grid.dividersAbove(ItemGrid.row(ref.gridIndex()))).orElse(List.of());
    }

    /** Puts the divider in the slot's grid in place of the one over the slot's column, if any; neighbours it overlaps are trimmed. */
    public static SetupContent withDivider(SetupContent content, SlotRef ref, Divider divider) {
        Objects.requireNonNull(divider, "divider");
        return changeGrid(content, ref, grid -> grid.dividerAt(ItemGrid.row(ref.gridIndex()), ItemGrid.column(ref.gridIndex()))
                .map(grid::withoutDivider).orElse(grid).withDivider(divider));
    }

    /** Drops the divider over the slot's column, if one spans it. */
    public static SetupContent withoutDividerAt(SetupContent content, SlotRef ref) {
        return changeGrid(content, ref, grid -> grid.dividerAt(ItemGrid.row(ref.gridIndex()), ItemGrid.column(ref.gridIndex()))
                .map(grid::withoutDivider).orElse(grid));
    }

    /** The grid a slot reference points into, through a cell when it has to. */
    public static Optional<ItemGrid> gridOf(SetupContent content, SlotRef ref) {
        Objects.requireNonNull(content, "content");
        Objects.requireNonNull(ref, "ref");
        if (!ref.inGrid()) {
            return Optional.empty();
        }
        if (ref instanceof SlotRef.Cell) {
            SlotRef.Cell cell = (SlotRef.Cell) ref;
            return cellOf(content, cell.cell()).filter(found -> found.kind() == CellKind.INVENTORY).map(LayoutCell::inventory);
        }
        return gridFor(content, ((SlotRef.Grid) ref).grid());
    }

    private static SetupContent changeGrid(SetupContent content, SlotRef ref, UnaryOperator<ItemGrid> change) {
        Objects.requireNonNull(content, "content");
        Objects.requireNonNull(ref, "ref");
        if (!ref.inGrid()) {
            return content;
        }
        if (ref instanceof SlotRef.Cell) {
            SlotRef.Cell cell = (SlotRef.Cell) ref;
            return replaceCell(content, cell.cell(), found -> found.kind() == CellKind.INVENTORY ? found.withInventory(change.apply(found.inventory())) : found);
        }
        return replaceGrid(content, ((SlotRef.Grid) ref).grid(), change);
    }

    /** Every item the content holds, in slot order. */
    public static List<SetupItem> allItems(SetupContent content) {
        return List.copyOf(Layout.of(Objects.requireNonNull(content, "content")).items());
    }

    /** Which slots a content shape offers; a reference to any other slot is ignored. */
    public static boolean supports(SetupContent content, SlotRef ref) {
        if (ref instanceof SlotRef.Cell) {
            SlotRef.Cell cell = (SlotRef.Cell) ref;
            return cellOf(content, cell.cell()).map(found -> found.supports(cell.inner())).orElse(false);
        }
        if (ref instanceof SlotRef.Equipment) {
            return content instanceof GearContent;
        }
        return gridFor(content, ((SlotRef.Grid) ref).grid()).isPresent();
    }

    private static Optional<LayoutCell> cellOf(SetupContent content, CellRef ref) {
        if (!(content instanceof CustomContent) || ref.row() >= ((CustomContent) content).rows()) {
            return Optional.empty();
        }
        return Optional.of(((CustomContent) content).cell(ref));
    }

    private static SetupContent replaceCell(SetupContent content, CellRef ref, UnaryOperator<LayoutCell> change) {
        return cellOf(content, ref)
                .map(cell -> (SetupContent) ((CustomContent) content).withCell(ref, change.apply(cell)))
                .orElse(content);
    }

    private static Optional<ItemGrid> gridFor(SetupContent content, GridKind kind) {
        if (content instanceof GearContent && kind == GridKind.INVENTORY) {
            return Optional.of(((GearContent) content).inventory());
        }
        if (content instanceof BankContent) {
            BankContent bank = (BankContent) content;
            switch (kind) {
                case LEFT:
                    return Optional.of(bank.left());
                case RIGHT:
                    return Optional.of(bank.right());
                default:
                    return Optional.empty();
            }
        }
        return Optional.empty();
    }

    private static SetupContent replaceGrid(SetupContent content, GridKind kind, UnaryOperator<ItemGrid> change) {
        if (content instanceof GearContent && kind == GridKind.INVENTORY) {
            GearContent gear = (GearContent) content;
            return gear.withInventory(change.apply(gear.inventory()));
        }
        if (content instanceof BankContent) {
            BankContent bank = (BankContent) content;
            switch (kind) {
                case LEFT:
                    return bank.withLeft(change.apply(bank.left()));
                case RIGHT:
                    return bank.withRight(change.apply(bank.right()));
                default:
                    return bank;
            }
        }
        return content;
    }
}
