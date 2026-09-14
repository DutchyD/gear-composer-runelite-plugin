package dev.dutchy.runelite.gear.ui;

import dev.dutchy.runelite.gear.content.GridKind;
import dev.dutchy.runelite.gear.content.ItemGrid;
import dev.dutchy.runelite.gear.content.SlotRef;
import dev.dutchy.runelite.libs.ui.icon.ItemIconFactory;
import net.runelite.client.ui.ColorScheme;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.UnaryOperator;

/** A four-wide grid of slots, with a strip of dividers above every row that has any. */
final class ItemGridView extends JPanel {

    private static final int GAP = 2;

    private final List<SlotView> slots = new ArrayList<>();
    private final List<DividerStrip> dividers = new ArrayList<>();

    ItemGridView(GridKind kind, ItemGrid grid, ItemIconFactory icons, SlotView.Listener listener) {
        this(kind, grid, icons, listener, UnaryOperator.identity());
    }

    /** {@code refs} turns a plain grid reference into the one the host expects, such as one inside a cell. */
    ItemGridView(GridKind kind, ItemGrid grid, ItemIconFactory icons, SlotView.Listener listener, UnaryOperator<SlotRef> refs) {
        Objects.requireNonNull(kind, "kind");
        Objects.requireNonNull(grid, "grid");
        Objects.requireNonNull(refs, "refs");
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(ColorScheme.DARK_GRAY_COLOR);
        for (int row = 0; row < ItemGrid.ROWS; row++) {
            SlotRef rowStart = refs.apply(SlotRef.of(kind, row * ItemGrid.COLUMNS));
            if (grid.hasDividerAbove(row)) {
                DividerStrip strip = new DividerStrip(rowStart, grid.dividersAbove(row), listener);
                dividers.add(strip);
                add(spaced(strip, row > 0 ? GAP : 0));
            }
            JPanel line = new JPanel(new GridLayout(1, ItemGrid.COLUMNS, GAP, GAP));
            line.setBackground(ColorScheme.DARK_GRAY_COLOR);
            for (int column = 0; column < ItemGrid.COLUMNS; column++) {
                int index = row * ItemGrid.COLUMNS + column;
                SlotRef ref = refs.apply(SlotRef.of(kind, index));
                SlotView slot = grid.slot(index)
                        .map(item -> new SlotView(ref, item, icons, listener))
                        .orElseGet(() -> new SlotView(ref, icons, listener));
                slots.add(slot);
                line.add(slot);
            }
            add(spaced(line, row > 0 ? GAP : 0));
        }
    }

    List<SlotView> slots() {
        return List.copyOf(slots);
    }

    List<DividerStrip> dividers() {
        return List.copyOf(dividers);
    }

    private static JPanel spaced(Component child, int gapAbove) {
        JPanel holder = new JPanel(new BorderLayout());
        holder.setBackground(ColorScheme.DARK_GRAY_COLOR);
        holder.setBorder(BorderFactory.createEmptyBorder(gapAbove, 0, 0, 0));
        holder.add(child, BorderLayout.CENTER);
        holder.setAlignmentX(LEFT_ALIGNMENT);
        Dimension preferred = child.getPreferredSize();
        holder.setMaximumSize(new Dimension(Integer.MAX_VALUE, preferred.height + gapAbove));
        return holder;
    }
}
