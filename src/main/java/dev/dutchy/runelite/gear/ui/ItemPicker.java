package dev.dutchy.runelite.gear.ui;

import dev.dutchy.runelite.libs.ui.icon.ItemIcon;
import dev.dutchy.runelite.libs.ui.icon.ItemIconFactory;
import dev.dutchy.runelite.libs.ui.item.ItemId;
import dev.dutchy.runelite.libs.ui.item.ItemReference;
import dev.dutchy.runelite.libs.ui.item.ResolvedItem;
import dev.dutchy.runelite.libs.ui.selector.ItemSelector;
import dev.dutchy.runelite.libs.ui.selector.ItemSelectorFactory;
import net.runelite.client.ui.ColorScheme;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;
import java.util.function.Consumer;

/** One card that both searches for an item and shows the chosen one: a sprite well beside the search field. */
final class ItemPicker extends Card {

    private static final int WELL = 36;
    private static final int SPRITE = 32;
    private static final int RESULT_LIMIT = 6;

    private final ItemIcon icon;
    private final ItemSelector selector;
    private final Card well = new Card();
    private final Consumer<ResolvedItem> onChosen;
    private final Runnable onCleared;

    private ItemId chosen;

    ItemPicker(ItemIconFactory icons, ItemSelectorFactory selectors, Consumer<ResolvedItem> onChosen, Runnable onCleared) {
        Objects.requireNonNull(icons, "icons");
        Objects.requireNonNull(selectors, "selectors");
        this.onChosen = Objects.requireNonNull(onChosen, "onChosen");
        this.onCleared = Objects.requireNonNull(onCleared, "onCleared");

        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        icon = icons.icon(SPRITE, SPRITE);
        icon.setResolvedListener(this::showResolved);

        well.setLayout(new BorderLayout());
        well.setBackground(ColorScheme.DARK_GRAY_COLOR);
        well.setPreferredSize(new Dimension(WELL, WELL));
        well.setMinimumSize(new Dimension(WELL, WELL));
        well.setToolTipText("The chosen item");
        well.add(icon, BorderLayout.CENTER);

        selector = selectors.selector()
                .limit(RESULT_LIMIT)
                .onSelect(this::choose)
                .build();
        selector.setLeadingComponent(well);
        selector.setBackground(getBackground());
        selector.addClearListener(this::onSelectorCleared);
        add(selector, BorderLayout.CENTER);
        refreshWell();
    }

    /** Shows an item picked elsewhere, for example the one a setup already has. */
    void show(ItemId item) {
        Objects.requireNonNull(item, "item");
        if (item.equals(chosen)) {
            return;
        }
        chosen = item;
        icon.setItem(ItemReference.byId(item));
        refreshWell();
    }

    void clear() {
        boolean hadChoice = chosen != null;
        chosen = null;
        icon.clearItem();
        refreshWell();
        if (hadChoice || !selector.query().isEmpty()) {
            selector.clear();
        }
    }

    @Override
    public boolean requestFocusInWindow() {
        return selector.requestFocusInWindow();
    }

    private void choose(ResolvedItem item) {
        chosen = item.id();
        if (icon.item().map(ResolvedItem::id).filter(item.id()::equals).isEmpty()) {
            icon.setItem(ItemReference.byId(item.id()));
        }
        refreshWell();
        onChosen.accept(item);
    }

    private void showResolved(ResolvedItem item) {
        if (item.id().equals(chosen) && selector.selectedItem().map(ResolvedItem::id).filter(item.id()::equals).isEmpty()) {
            selector.select(item);
        }
    }

    private void onSelectorCleared() {
        if (chosen == null) {
            return;
        }
        chosen = null;
        icon.clearItem();
        refreshWell();
        onCleared.run();
    }

    private void refreshWell() {
        well.setOutline(chosen != null ? ColorScheme.BRAND_ORANGE : Ui.OUTLINE);
    }
}
