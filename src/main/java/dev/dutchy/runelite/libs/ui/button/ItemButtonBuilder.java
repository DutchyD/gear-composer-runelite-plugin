package dev.dutchy.runelite.libs.ui.button;

import dev.dutchy.runelite.libs.ui.image.ImageTransform;
import dev.dutchy.runelite.libs.ui.image.ItemImageOptions;
import dev.dutchy.runelite.libs.ui.item.ItemReference;
import dev.dutchy.runelite.libs.ui.style.ItemButtonStyle;
import dev.dutchy.runelite.libs.ui.swing.EdtDispatch;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class ItemButtonBuilder {

    private final ItemLoader loader;
    private final List<ItemClickListener> clickListeners = new ArrayList<>();

    private ItemReference reference;
    private ItemButtonStyle style;
    private ImageTransform transform = ImageTransform.identity();
    private ItemImageOptions imageOptions = ItemImageOptions.DEFAULT;
    private String tooltip;
    private boolean selected;
    private boolean toggle;

    ItemButtonBuilder(ItemLoader loader, ItemButtonStyle defaultStyle, ItemReference reference) {
        this.loader = Objects.requireNonNull(loader, "loader");
        this.style = Objects.requireNonNull(defaultStyle, "defaultStyle");
        this.reference = reference;
    }

    public ItemButtonBuilder item(ItemReference newReference) {
        this.reference = Objects.requireNonNull(newReference, "newReference");
        return this;
    }

    public ItemButtonBuilder style(ItemButtonStyle newStyle) {
        this.style = Objects.requireNonNull(newStyle, "newStyle");
        return this;
    }

    public ItemButtonBuilder transform(ImageTransform newTransform) {
        this.transform = Objects.requireNonNull(newTransform, "newTransform");
        return this;
    }

    public ItemButtonBuilder quantity(int quantity) {
        this.imageOptions = imageOptions.withQuantity(quantity);
        return this;
    }

    public ItemButtonBuilder tooltip(String text) {
        this.tooltip = text;
        return this;
    }

    public ItemButtonBuilder selected(boolean isSelected) {
        this.selected = isSelected;
        return this;
    }

    public ItemButtonBuilder toggle(boolean isToggle) {
        this.toggle = isToggle;
        return this;
    }

    public ItemButtonBuilder onClick(ItemClickListener listener) {
        clickListeners.add(Objects.requireNonNull(listener, "listener"));
        return this;
    }

    public ItemButton build() {
        EdtDispatch.requireEdt();
        ItemButton button = new ItemButton(loader, style, transform, imageOptions);
        button.setCustomTooltip(tooltip);
        button.setSelected(selected);
        if (toggle) {
            button.addItemClickListener(event -> {
                if (event.isLeftClick()) {
                    button.setSelected(!button.isSelected());
                }
            });
        }
        clickListeners.forEach(button::addItemClickListener);
        if (reference != null) {
            button.setItem(reference);
        }
        return button;
    }
}
