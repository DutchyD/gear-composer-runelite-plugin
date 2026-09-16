package dev.dutchy.runelite.libs.ui.selector;

import dev.dutchy.runelite.libs.ui.button.ItemButtonFactory;
import dev.dutchy.runelite.libs.ui.search.ItemSearch;
import dev.dutchy.runelite.libs.ui.style.ItemButtonStyle;
import dev.dutchy.runelite.libs.ui.swing.EdtDispatch;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class ItemSelectorBuilder {

    public static final int DEFAULT_LIMIT = 8;
    public static final int DEFAULT_DEBOUNCE_MILLIS = 150;

    private final ItemSearch search;
    private final ItemButtonFactory buttons;
    private final List<ItemSelectionListener> listeners = new ArrayList<>();

    private int limit = DEFAULT_LIMIT;

    private ItemRowDragListener rowDrag;
    private boolean keepResultsOnSelect;

    ItemSelectorBuilder(ItemSearch search, ItemButtonFactory buttons) {
        this.search = Objects.requireNonNull(search, "search");
        this.buttons = Objects.requireNonNull(buttons, "buttons");
    }

    public ItemSelectorBuilder limit(int maxResults) {
        if (maxResults <= 0) {
            throw new IllegalArgumentException("Limit must be positive, got " + maxResults);
        }
        this.limit = maxResults;
        return this;
    }

    /** Lets result rows be dragged; the host decides what a drop means. */
    public ItemSelectorBuilder onRowDrag(ItemRowDragListener listener) {
        this.rowDrag = Objects.requireNonNull(listener, "listener");
        return this;
    }

    /** Selecting leaves the result list in place, so rows stay there to be dragged or picked again. */
    public ItemSelectorBuilder keepResultsOnSelect() {
        this.keepResultsOnSelect = true;
        return this;
    }

    public ItemSelectorBuilder onSelect(ItemSelectionListener listener) {
        listeners.add(Objects.requireNonNull(listener, "listener"));
        return this;
    }

    public ItemSelector build() {
        EdtDispatch.requireEdt();
        ItemSelector selector = new ItemSelector(search, buttons, ItemButtonStyle.compact(), limit,
                DEFAULT_DEBOUNCE_MILLIS, rowDrag, keepResultsOnSelect);
        listeners.forEach(selector::addSelectionListener);
        return selector;
    }
}
