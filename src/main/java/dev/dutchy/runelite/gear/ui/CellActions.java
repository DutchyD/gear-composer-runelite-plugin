package dev.dutchy.runelite.gear.ui;

import dev.dutchy.runelite.gear.content.CellKind;
import dev.dutchy.runelite.gear.content.CellRef;

/** What a cell of a custom layout can ask of its host. */
interface CellActions {

    void setCellKind(CellRef ref, CellKind kind);

    /** Opens the picker that copies another setup's equipment or inventory into the cell. */
    void fillCellFromSetup(CellRef ref);

    void fillCellFromGame(CellRef ref);

    void renameCell(CellRef ref);

    /** Keeps the cell's kind and name, drops its items. */
    void clearCell(CellRef ref);

    /** Turns the cell back into an empty one. */
    void emptyCell(CellRef ref);
}
