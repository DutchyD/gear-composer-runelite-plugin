package dev.dutchy.runelite.libs.ui.image;

import dev.dutchy.runelite.libs.ui.item.ItemId;
import lombok.Value;
import lombok.experimental.Accessors;

@Value
@Accessors(fluent = true)
public class ItemImageOptions {
    int quantity;
    boolean showQuantity;

    public static final ItemImageOptions DEFAULT = new ItemImageOptions(ItemImageRequest.DEFAULT_QUANTITY, false);

    public ItemImageOptions(int quantity, boolean showQuantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("Quantity must be non-negative, got " + quantity);
        }
        this.quantity = quantity;
        this.showQuantity = showQuantity;
    }

    public ItemImageOptions withQuantity(int newQuantity) {
        return new ItemImageOptions(newQuantity, showQuantity);
    }

    public ItemImageOptions withShowQuantity(boolean show) {
        return new ItemImageOptions(quantity, show);
    }

    public ItemImageRequest toRequest(ItemId itemId) {
        return new ItemImageRequest(itemId, quantity, showQuantity);
    }
}
