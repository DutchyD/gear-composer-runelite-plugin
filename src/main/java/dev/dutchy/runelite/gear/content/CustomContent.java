package dev.dutchy.runelite.gear.content;

import lombok.Value;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** A grid of cells two wide and one to three tall, each an equipment set, an inventory, or nothing. */
@Value
public class CustomContent implements SetupContent {
    int rows;
    List<LayoutCell> cells;

    public static final int COLUMNS = 2;
    public static final int MIN_ROWS = 1;
    public static final int MAX_ROWS = 3;
    public static final int DEFAULT_ROWS = 2;

    public CustomContent(int rows, List<LayoutCell> cells) {
        requireValidRows(rows);
        Objects.requireNonNull(cells, "cells");
        if (cells.size() != rows * COLUMNS) {
            throw new IllegalArgumentException(rows + " rows need " + rows * COLUMNS + " cells, got " + cells.size());
        }
        this.rows = rows;
        this.cells = List.copyOf(cells);
    }

    public static CustomContent empty(int rows) {
        requireValidRows(rows);
        List<LayoutCell> cells = new ArrayList<>();
        for (int i = 0; i < rows * COLUMNS; i++) {
            cells.add(LayoutCell.EMPTY);
        }
        return new CustomContent(rows, cells);
    }

    public static CustomContent empty() {
        return empty(DEFAULT_ROWS);
    }

    @Override
    public SetupType type() {
        return SetupType.CUSTOM;
    }

    @Override
    public boolean isEmpty() {
        return cells.stream().allMatch(LayoutCell::isEmpty);
    }

    public LayoutCell cell(CellRef ref) {
        return cells.get(indexOf(ref));
    }

    /** Every position in reading order. */
    public List<CellRef> cellRefs() {
        List<CellRef> refs = new ArrayList<>();
        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < COLUMNS; column++) {
                refs.add(CellRef.of(row, column));
            }
        }
        return List.copyOf(refs);
    }

    public CustomContent withCell(CellRef ref, LayoutCell cell) {
        List<LayoutCell> updated = new ArrayList<>(cells);
        updated.set(indexOf(ref), Objects.requireNonNull(cell, "cell"));
        return new CustomContent(rows, updated);
    }

    /** Grows with empty cells or drops the bottom rows. */
    public CustomContent withRows(int newRows) {
        requireValidRows(newRows);
        List<LayoutCell> updated = new ArrayList<>(cells.subList(0, Math.min(cells.size(), newRows * COLUMNS)));
        while (updated.size() < newRows * COLUMNS) {
            updated.add(LayoutCell.EMPTY);
        }
        return new CustomContent(newRows, updated);
    }

    /** Whether shrinking to {@code newRows} would drop any items. */
    public boolean hasItemsBelow(int newRows) {
        return cells.stream().skip((long) newRows * COLUMNS).anyMatch(cell -> !cell.isEmpty());
    }

    public List<SetupItem> allItems() {
        List<SetupItem> items = new ArrayList<>();
        cells.forEach(cell -> items.addAll(cell.items()));
        return List.copyOf(items);
    }

    private int indexOf(CellRef ref) {
        Objects.requireNonNull(ref, "ref");
        if (ref.row() >= rows) {
            throw new IllegalArgumentException("No row " + (ref.row() + 1) + " in a " + rows + " row layout");
        }
        return ref.row() * COLUMNS + ref.column();
    }

    private static void requireValidRows(int rows) {
        if (rows < MIN_ROWS || rows > MAX_ROWS) {
            throw new IllegalArgumentException("Rows must be within [" + MIN_ROWS + ", " + MAX_ROWS + "], got " + rows);
        }
    }
}
