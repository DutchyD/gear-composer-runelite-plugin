package dev.dutchy.runelite.gear.layout;

import dev.dutchy.runelite.gear.content.SetupItem;
import lombok.Value;

import java.util.Objects;

@Value
public class BankPlacement {
    BankSide side;
    int column;
    int row;
    SetupItem item;

    public BankPlacement(BankSide side, int column, int row, SetupItem item) {
        Objects.requireNonNull(side, "side");
        Objects.requireNonNull(item, "item");
        if (column < 0 || row < 0) {
            throw new IllegalArgumentException("Placement must be at non-negative coordinates, got " + column + "," + row);
        }
        this.side = side;
        this.column = column;
        this.row = row;
        this.item = item;
    }
}
