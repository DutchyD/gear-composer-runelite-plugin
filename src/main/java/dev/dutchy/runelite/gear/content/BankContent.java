package dev.dutchy.runelite.gear.content;

import lombok.Value;
import lombok.experimental.Accessors;
import java.util.Objects;

@Value
@Accessors(fluent = true)
public class BankContent implements SetupContent {
    ItemGrid left;
    ItemGrid right;

    public BankContent(ItemGrid left, ItemGrid right) {
        Objects.requireNonNull(left, "left");
        Objects.requireNonNull(right, "right");
        this.left = left;
        this.right = right;
    }

    public static BankContent empty() {
        return new BankContent(ItemGrid.EMPTY, ItemGrid.EMPTY);
    }

    @Override
    public SetupType type() {
        return SetupType.BANK;
    }

    @Override
    public boolean isEmpty() {
        return left.isEmpty() && right.isEmpty();
    }

    public BankContent withLeft(ItemGrid newLeft) {
        return new BankContent(newLeft, right);
    }

    public BankContent withRight(ItemGrid newRight) {
        return new BankContent(left, newRight);
    }
}
