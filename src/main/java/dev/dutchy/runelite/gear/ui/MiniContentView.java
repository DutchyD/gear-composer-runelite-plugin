package dev.dutchy.runelite.gear.ui;

import dev.dutchy.runelite.gear.GearSetup;
import dev.dutchy.runelite.gear.layout.Layout;
import dev.dutchy.runelite.libs.ui.icon.ItemIconFactory;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;

/** A setup's chosen variant at a glance, for the hover preview beside its tile. */
final class MiniContentView extends Card {

    static final int CELL = 18;

    MiniContentView(GearSetup setup, ItemIconFactory icons) {
        Objects.requireNonNull(setup, "setup");
        Objects.requireNonNull(icons, "icons");
        setLayout(new BorderLayout(0, Ui.SMALL_GAP));
        setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));

        JLabel title = new JLabel(setup.hasVariants() ? setup.name() + " · " + setup.variant().name() : setup.name());
        title.setFont(FontManager.getRunescapeBoldFont());
        title.setForeground(ColorScheme.BRAND_ORANGE);

        int count = Layout.of(setup.content()).items().size();
        String items = count == 1 ? "1 item" : count + " items";
        JLabel footer = Ui.hint(setup.hasVariants() ? items + " · " + setup.variants().size() + " variants" : items);

        add(title, BorderLayout.NORTH);
        add(new MiniLayoutView(setup.content(), icons, CELL), BorderLayout.CENTER);
        add(footer, BorderLayout.SOUTH);
    }
}
