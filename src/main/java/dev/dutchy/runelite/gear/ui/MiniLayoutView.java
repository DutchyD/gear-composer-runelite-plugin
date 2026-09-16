package dev.dutchy.runelite.gear.ui;

import dev.dutchy.runelite.gear.content.EquipmentSlot;
import dev.dutchy.runelite.gear.content.ItemGrid;
import dev.dutchy.runelite.gear.content.SetupContent;
import dev.dutchy.runelite.gear.content.SetupItem;
import dev.dutchy.runelite.gear.layout.*;
import dev.dutchy.runelite.libs.ui.icon.ItemIcon;
import dev.dutchy.runelite.libs.ui.icon.ItemIconFactory;
import dev.dutchy.runelite.libs.ui.item.ItemReference;
import net.runelite.client.ui.ColorScheme;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/** A layout's items at a glance: tiny sprites in the real slot arrangement, one row per band, nothing clickable. */
final class MiniLayoutView extends JPanel {

    private final int cell;
    private final int sprite;
    private final ItemIconFactory icons;

    MiniLayoutView(SetupContent content, ItemIconFactory icons, int cell) {
        Objects.requireNonNull(content, "content");
        this.icons = Objects.requireNonNull(icons, "icons");
        this.cell = cell;
        this.sprite = cell - 2;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setOpaque(false);
        for (Band band : Layout.of(content).bands()) {
            JPanel row = Ui.panel(new FlowLayout(FlowLayout.CENTER, Ui.SMALL_GAP, 0));
            for (LayoutBlock block : band.blocks()) {
                row.add(block.accept(new LayoutBlock.Visitor<JComponent>() {
                    @Override
                    public JComponent equipment(EquipmentBlock equipment) {
                        return crossOf(equipment.equipment());
                    }

                    @Override
                    public JComponent grid(GridBlock grid) {
                        return gridOf(grid.grid());
                    }
                }));
            }
            add(row);
        }
    }

    private JComponent crossOf(Map<EquipmentSlot, SetupItem> equipment) {
        Map<Integer, EquipmentSlot> byPosition = new HashMap<>();
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            byPosition.put(slot.row() * EquipmentSlot.COLUMNS + slot.column(), slot);
        }
        JPanel panel = Ui.panel(new GridLayout(EquipmentSlot.ROWS, EquipmentSlot.COLUMNS, 1, 1));
        for (int position = 0; position < EquipmentSlot.ROWS * EquipmentSlot.COLUMNS; position++) {
            EquipmentSlot slot = byPosition.get(position);
            panel.add(slot == null ? blank() : cellOf(equipment.get(slot)));
        }
        return panel;
    }

    private JComponent gridOf(ItemGrid grid) {
        JPanel panel = Ui.panel(new GridLayout(ItemGrid.ROWS, ItemGrid.COLUMNS, 1, 1));
        for (int index = 0; index < ItemGrid.SIZE; index++) {
            panel.add(cellOf(grid.slot(index).orElse(null)));
        }
        return panel;
    }

    /** {@code item} is what the slot holds, null when it is empty. */
    private JComponent cellOf(SetupItem item) {
        JPanel box = new JPanel(new BorderLayout());
        box.setBackground(ColorScheme.DARK_GRAY_COLOR);
        box.setPreferredSize(new Dimension(cell, cell));
        if (item != null) {
            ItemIcon icon = icons.icon(sprite, sprite);
            icon.setItem(ItemReference.byId(item.id()));
            box.add(icon, BorderLayout.CENTER);
        }
        return box;
    }

    private JComponent blank() {
        JPanel filler = Ui.panel(new BorderLayout());
        filler.setPreferredSize(new Dimension(cell, cell));
        return filler;
    }
}
