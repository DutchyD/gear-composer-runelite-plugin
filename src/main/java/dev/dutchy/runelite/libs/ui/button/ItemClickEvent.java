package dev.dutchy.runelite.libs.ui.button;

import dev.dutchy.runelite.libs.ui.item.ResolvedItem;
import lombok.Value;
import lombok.experimental.Accessors;

import java.awt.event.MouseEvent;
import java.util.Objects;

@Value
@Accessors(fluent = true)
public class ItemClickEvent {
    ItemButton source;
    ResolvedItem item;
    MouseButton button;
    int clickCount;
    MouseEvent mouseEvent;

    public ItemClickEvent(ItemButton source, ResolvedItem item, MouseButton button, int clickCount, MouseEvent mouseEvent) {
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(item, "item");
        Objects.requireNonNull(button, "button");
        Objects.requireNonNull(mouseEvent, "mouseEvent");
        if (clickCount < 1) {
            throw new IllegalArgumentException("Click count must be at least 1, got " + clickCount);
        }
        this.source = source;
        this.item = item;
        this.button = button;
        this.clickCount = clickCount;
        this.mouseEvent = mouseEvent;
    }

    public boolean isLeftClick() {
        return button == MouseButton.LEFT;
    }

    public boolean isRightClick() {
        return button == MouseButton.RIGHT;
    }

    public boolean isDoubleClick() {
        return clickCount == 2;
    }

}
