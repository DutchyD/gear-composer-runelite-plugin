package dev.dutchy.runelite.gear.layout;

import dev.dutchy.runelite.gear.content.SetupItem;
import dev.dutchy.runelite.gear.content.SlotRef;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * One piece of a layout with a fixed shape: an equipment cross or an item grid.
 * A block knows its band, its side of the bank, the bank row its items start on, and the row its
 * title is painted on when it has one.
 */
public abstract class LayoutBlock {

    /** How a consumer tells the shapes apart without an instanceof chain. */
    public interface Visitor<R> {
        R equipment(EquipmentBlock block);

        R grid(GridBlock block);
    }

    /** One slot of a block: where it is and what it holds. */
    public static final class Slot {
        private final SlotRef ref;
        private final SetupItem item;

        Slot(SlotRef ref, SetupItem item) {
            this.ref = Objects.requireNonNull(ref, "ref");
            this.item = item;
        }

        public SlotRef ref() {
            return ref;
        }

        public Optional<SetupItem> item() {
            return Optional.ofNullable(item);
        }
    }

    private final String title;
    private final int band;
    private final BankSide side;
    private final int firstRow;
    private final Integer labelRow;

    LayoutBlock(String title, int band, BankSide side, int firstRow, Integer labelRow) {
        this.title = Objects.requireNonNull(title, "title");
        this.band = band;
        this.side = Objects.requireNonNull(side, "side");
        this.firstRow = firstRow;
        this.labelRow = labelRow;
    }

    public String title() {
        return title;
    }

    /** Blocks in the same band sit side by side; bands stack top to bottom. */
    public int band() {
        return band;
    }

    public BankSide side() {
        return side;
    }

    /** The bank row the block's first item row lands on. */
    public int firstRow() {
        return firstRow;
    }

    /** The bank row the title is painted on, when the block has one. */
    public Optional<Integer> labelRow() {
        return Optional.ofNullable(labelRow);
    }

    /** Bank rows the block's items take. */
    public abstract int rowsTall();

    /** Bank rows of this block that carry a header strip above their items. */
    public List<Integer> headerRows() {
        return List.of();
    }

    /** Columns the block takes on its side of the bank. */
    public abstract int columnsWide();

    /** Every slot of the block, filled or not, in reading order. */
    public abstract List<Slot> slots();

    public abstract List<BankPlacement> placements();

    public abstract <R> R accept(Visitor<R> visitor);

    /** Every item the block holds, in slot order. */
    public List<SetupItem> items() {
        return slots().stream().filter(slot -> slot.item().isPresent()).map(slot -> slot.item().get()).collect(Collectors.toList());
    }

    public boolean isEmpty() {
        return items().isEmpty();
    }

    List<LayoutLabel> labels() {
        return labelRow == null ? List.of() : List.of(new LayoutLabel(side, labelRow, title));
    }
}
