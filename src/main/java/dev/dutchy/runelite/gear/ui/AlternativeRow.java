package dev.dutchy.runelite.gear.ui;

import dev.dutchy.runelite.libs.ui.icon.ItemIcon;
import dev.dutchy.runelite.libs.ui.icon.ItemIconFactory;
import dev.dutchy.runelite.libs.ui.item.ItemId;
import dev.dutchy.runelite.libs.ui.item.ItemReference;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;
import java.util.function.Consumer;

/** One acceptable stand-in for a slot's item, with a way to drop it again. */
final class AlternativeRow extends Card {

    private static final int SPRITE = 22;
    private static final int HEIGHT = 30;

    private final ItemId item;

    AlternativeRow(int rank, ItemId item, ItemIconFactory icons, Consumer<ItemId> onRemove) {
        this.item = Objects.requireNonNull(item, "item");
        Objects.requireNonNull(icons, "icons");
        Objects.requireNonNull(onRemove, "onRemove");
        setLayout(new BorderLayout(6, 0));
        setBorder(BorderFactory.createEmptyBorder(3, 6, 3, 2));
        setPreferredSize(new Dimension(0, HEIGHT));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, HEIGHT));

        JLabel name = Ui.body("Loading…");
        ItemIcon icon = icons.icon(SPRITE, SPRITE);
        icon.setResolvedListener(resolved -> name.setText(resolved.name()));
        icon.setItem(ItemReference.byId(item));

        JLabel order = Ui.hint(rank + ".");
        order.setPreferredSize(new Dimension(14, SPRITE));

        add(Ui.column(0, order), BorderLayout.WEST);
        add(withIcon(icon, name), BorderLayout.CENTER);
        add(new FlatButton(ActionIcon.DELETE, "Stop accepting this item", () -> onRemove.accept(item)), BorderLayout.EAST);
    }

    private static JPanel withIcon(ItemIcon icon, JLabel name) {
        JPanel row = Ui.panel(new BorderLayout(6, 0));
        row.add(icon, BorderLayout.WEST);
        row.add(name, BorderLayout.CENTER);
        return row;
    }
}
