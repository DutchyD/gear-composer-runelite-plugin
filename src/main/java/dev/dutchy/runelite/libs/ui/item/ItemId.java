package dev.dutchy.runelite.libs.ui.item;

import lombok.Value;
@Value
public class ItemId implements Comparable<ItemId> {
    int value;

    public ItemId(int value) {
        if (value < 0) {
            throw new IllegalArgumentException("Item id must be non-negative, got " + value);
        }
        this.value = value;
    }

    public static ItemId of(int value) {
        return new ItemId(value);
    }

    @Override
    public int compareTo(ItemId other) {
        return Integer.compare(value, other.value);
    }

    @Override
    public String toString() {
        return "#" + value;
    }
}
