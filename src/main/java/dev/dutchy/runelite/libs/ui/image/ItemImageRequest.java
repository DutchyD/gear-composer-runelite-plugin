package dev.dutchy.runelite.libs.ui.image;

import dev.dutchy.runelite.libs.ui.item.ItemId;
import lombok.Value;
import lombok.experimental.Accessors;

import java.util.Objects;

@Value
@Accessors(fluent = true)
public class ItemImageRequest {
    ItemId itemId;
    int quantity;
    boolean showQuantity;

    public static final int DEFAULT_QUANTITY = 1;

    public ItemImageRequest(ItemId itemId, int quantity, boolean showQuantity) {
        Objects.requireNonNull(itemId, "itemId");
        if (quantity < 0) {
            throw new IllegalArgumentException("Quantity must be non-negative, got " + quantity);
        }
        this.itemId = itemId;
        this.quantity = quantity;
        this.showQuantity = showQuantity;
    }

    public static ItemImageRequest of(ItemId itemId) {
        return new ItemImageRequest(itemId, DEFAULT_QUANTITY, false);
    }
}
