package dev.dutchy.runelite.gear.ui;

import dev.dutchy.runelite.gear.content.CellRef;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Value;
import lombok.With;
import lombok.experimental.Accessors;

import java.util.Objects;
import java.util.Optional;

/** Where the contents page is looking: the row on show, the marked cell, and whether the variants, ledger and finder are open. */
@Value
@Accessors(fluent = true)
@With
class ContentViewState {

    int row;
    @Getter(AccessLevel.NONE)
    CellRef cell;
    boolean variantsOpen;
    boolean ledgerOpen;
    boolean finderOpen;
    String finderQuery;

    ContentViewState(int row, CellRef cell, boolean variantsOpen, boolean ledgerOpen, boolean finderOpen, String finderQuery) {
        this.row = Math.max(0, row);
        this.cell = cell;
        this.variantsOpen = variantsOpen;
        this.ledgerOpen = ledgerOpen;
        this.finderOpen = finderOpen;
        this.finderQuery = Objects.requireNonNull(finderQuery, "finderQuery");
    }

    static ContentViewState initial() {
        return new ContentViewState(0, null, true, false, false, "");
    }

    Optional<CellRef> cell() {
        return Optional.ofNullable(cell);
    }

    /** Marks the cell and switches to its row. */
    ContentViewState at(CellRef ref) {
        return new ContentViewState(ref.row(), ref, variantsOpen, ledgerOpen, finderOpen, finderQuery);
    }

    ContentViewState withFinder(boolean open, String query) {
        return new ContentViewState(row, cell, variantsOpen, ledgerOpen, open, query);
    }
}
