package dev.dutchy.runelite.gear.ui;

import dev.dutchy.runelite.gear.GearSetup;
import dev.dutchy.runelite.gear.SectionId;
import dev.dutchy.runelite.libs.ui.icon.ItemIconFactory;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

final class SetupGrid extends JPanel {

    static final int COLUMNS = 4;
    private static final int GAP = 4;
    private static final int EMPTY_HEIGHT = 34;

    private final SectionId sectionId;
    private final TileStyle style;
    private final List<SetupTile> tiles = new ArrayList<>();
    private int caretIndex = -1;

    SetupGrid(SectionId sectionId, List<GearSetup> setups, ItemIconFactory icons, SetupActions actions,
              SetupDragController drags, BulkSelector bulk, TileStyle style, HoverPreview preview) {
        this(sectionId, setups, icons, actions, drags, bulk, style, preview, true);
    }

    /** {@code showEmptyHint} is off for a section whose sub-sections hold its setups, so it does not read as empty. */
    SetupGrid(SectionId sectionId, List<GearSetup> setups, ItemIconFactory icons, SetupActions actions,
              SetupDragController drags, BulkSelector bulk, TileStyle style, HoverPreview preview, boolean showEmptyHint) {
        this.sectionId = Objects.requireNonNull(sectionId, "sectionId");
        this.style = Objects.requireNonNull(style, "style");
        Objects.requireNonNull(setups, "setups");
        setLayout(new GridLayout(0, style.columns(), GAP, GAP));
        setBackground(ColorScheme.DARK_GRAY_COLOR);

        for (GearSetup setup : setups) {
            SetupTile tile = new SetupTile(setup, icons, actions, drags, bulk, style, preview);
            tiles.add(tile);
            add(tile);
        }
        if (tiles.isEmpty() && showEmptyHint) {
            add(emptyHint());
        }
    }

    List<SetupTile> tiles() {
        return List.copyOf(tiles);
    }

    SectionId sectionId() {
        return sectionId;
    }

    int columns() {
        return style.columns();
    }

    int insertionIndexAt(Point point) {
        List<Rectangle> bounds = tiles.stream().map(SetupTile::getBounds).collect(Collectors.toList());
        return DropIndex.insertionIndex(bounds, point);
    }

    void showCaret(int index) {
        setCaret(index);
    }

    void clearCaret() {
        setCaret(-1);
    }

    private void setCaret(int index) {
        if (caretIndex != index) {
            caretIndex = index;
            repaint();
        }
    }

    private JLabel emptyHint() {
        JLabel hint = new JLabel("Empty", SwingConstants.CENTER);
        hint.setToolTipText("Drop a setup here, or add one with the + on the section");
        hint.setFont(FontManager.getRunescapeSmallFont());
        hint.setForeground(ColorScheme.MEDIUM_GRAY_COLOR);
        hint.setPreferredSize(new Dimension(SetupTile.WIDTH, EMPTY_HEIGHT));
        return hint;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (caretIndex < 0 || tiles.isEmpty()) {
            return;
        }
        Rectangle bounds = caretIndex < tiles.size()
                ? tiles.get(caretIndex).getBounds()
                : tiles.get(tiles.size() - 1).getBounds();
        int x = caretIndex < tiles.size() ? bounds.x - GAP / 2 : bounds.x + bounds.width + GAP / 2;
        g.setColor(ColorScheme.BRAND_ORANGE);
        g.fillRect(Math.max(0, x - 1), bounds.y, 2, bounds.height);
    }
}
