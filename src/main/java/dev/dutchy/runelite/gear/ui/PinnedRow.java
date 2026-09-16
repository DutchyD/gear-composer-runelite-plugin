package dev.dutchy.runelite.gear.ui;

import dev.dutchy.runelite.gear.GearSetup;
import dev.dutchy.runelite.gear.SectionId;
import dev.dutchy.runelite.libs.ui.icon.ItemIconFactory;
import net.runelite.client.ui.ColorScheme;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/** The starred setups, always at the top, never dragged or bulk-selected. */
final class PinnedRow extends JPanel {

    static final int CAPACITY = SetupCommands.MAX_PINNED;
    static final String CAPTION = "Pinned";

    private final SetupGrid grid;

    PinnedRow(List<GearSetup> pinned, ItemIconFactory icons, SetupActions actions, TileStyle style, HoverPreview preview) {
        Objects.requireNonNull(pinned, "pinned");
        setLayout(new BorderLayout(0, Ui.SMALL_GAP));
        setBackground(ColorScheme.DARK_GRAY_COLOR);
        setBorder(BorderFactory.createEmptyBorder(0, 0, Ui.GAP, 0));
        setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel caption = Ui.caption(CAPTION);
        caption.setIcon(ActionIcon.PIN);
        caption.setForeground(ColorScheme.BRAND_ORANGE);
        caption.setIconTextGap(5);
        caption.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Ui.OUTLINE),
                BorderFactory.createEmptyBorder(0, 2, 3, 0)));

        grid = new SetupGrid(new SectionId(UUID.nameUUIDFromBytes("pinned".getBytes())), pinned, icons, actions, null, null, style, preview);
        add(caption, BorderLayout.NORTH);
        add(grid, BorderLayout.CENTER);
    }

    SetupGrid grid() {
        return grid;
    }
}
