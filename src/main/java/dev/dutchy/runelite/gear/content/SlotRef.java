package dev.dutchy.runelite.gear.content;

import lombok.Value;
import java.util.Objects;

public interface SlotRef {

    String describe();

    /** Whether the slot sits in an item grid, so it can name its neighbours. */
    default boolean inGrid() {
        return false;
    }

    default int gridIndex() {
        throw new IllegalStateException(describe() + " is not a grid slot");
    }

    /** The slot at {@code index} in the same grid as this one. */
    default SlotRef gridSibling(int index) {
        throw new IllegalStateException(describe() + " is not a grid slot");
    }

    static SlotRef of(EquipmentSlot slot) {
        return new Equipment(slot);
    }

    static SlotRef of(GridKind grid, int index) {
        return new Grid(grid, index);
    }

    static SlotRef in(CellRef cell, SlotRef inner) {
        return new Cell(cell, inner);
    }

    /** A slot inside one cell of a custom layout; the inner reference is an equipment slot or an inventory slot. */
    @Value
    class Cell implements SlotRef {
        CellRef cell;
        SlotRef inner;

        public Cell(CellRef cell, SlotRef inner) {
            this.cell = Objects.requireNonNull(cell, "cell");
            this.inner = Objects.requireNonNull(inner, "inner");
            if (!(inner instanceof Equipment) && !(inner instanceof Grid)) {
                throw new IllegalArgumentException("A cell holds equipment or inventory slots, not " + inner.describe());
            }
        }

        @Override
        public String describe() {
            return cell.describe() + ", " + inner.describe();
        }

        @Override
        public boolean inGrid() {
            return inner.inGrid();
        }

        @Override
        public int gridIndex() {
            return inner.gridIndex();
        }

        @Override
        public SlotRef gridSibling(int index) {
            return new Cell(cell, inner.gridSibling(index));
        }
    }

    @Value
    class Equipment implements SlotRef {
        EquipmentSlot slot;


        public Equipment(EquipmentSlot slot) {
            Objects.requireNonNull(slot, "slot");
            this.slot = slot;
        }

        @Override
        public String describe() {
            return slot.displayName();
        }
    }

    @Value
    class Grid implements SlotRef {
        GridKind grid;
        int index;


        public Grid(GridKind grid, int index) {
            Objects.requireNonNull(grid, "grid");
            if (index < 0 || index >= ItemGrid.SIZE) {
                throw new IllegalArgumentException("Slot index must be within [0, " + ItemGrid.SIZE + "), got " + index);
            }
            this.grid = grid;
            this.index = index;
        }

        @Override
        public String describe() {
            return grid.displayName() + " slot " + (index + 1);
        }

        @Override
        public boolean inGrid() {
            return true;
        }

        @Override
        public int gridIndex() {
            return index;
        }

        @Override
        public SlotRef gridSibling(int newIndex) {
            return new Grid(grid, newIndex);
        }
    }
}
