package dev.dutchy.runelite.gear.ui;

import dev.dutchy.runelite.gear.content.EquipmentSlot;
import dev.dutchy.runelite.gear.content.GearContent;
import dev.dutchy.runelite.gear.content.SetupItem;
import dev.dutchy.runelite.gear.content.SlotRef;
import dev.dutchy.runelite.libs.ui.icon.ItemIconFactory;
import net.runelite.client.ui.ColorScheme;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.function.UnaryOperator;

final class EquipmentView extends JPanel {

    private static final int GAP = 2;

    private final List<SlotView> slots = new ArrayList<>();
    private final UnaryOperator<SlotRef> refs;

    EquipmentView(GearContent gear, ItemIconFactory icons, EquipmentSlotArtwork artwork, SlotView.Listener listener) {
        this(Objects.requireNonNull(gear, "gear").equipment(), icons, artwork, listener, UnaryOperator.identity());
    }

    /** {@code refs} turns a plain equipment reference into the one the host expects, such as one inside a cell. */
    EquipmentView(Map<EquipmentSlot, SetupItem> equipment, ItemIconFactory icons, EquipmentSlotArtwork artwork, SlotView.Listener listener,
                  UnaryOperator<SlotRef> refs) {
        Objects.requireNonNull(equipment, "equipment");
        Objects.requireNonNull(artwork, "artwork");
        Objects.requireNonNull(refs, "refs");
        this.refs = refs;
        setLayout(new GridLayout(EquipmentSlot.ROWS, EquipmentSlot.COLUMNS, GAP, GAP));
        setBackground(ColorScheme.DARK_GRAY_COLOR);

        Map<Integer, EquipmentSlot> byPosition = new HashMap<>();
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            byPosition.put(slot.row() * EquipmentSlot.COLUMNS + slot.column(), slot);
        }
        for (int position = 0; position < EquipmentSlot.ROWS * EquipmentSlot.COLUMNS; position++) {
            EquipmentSlot slot = byPosition.get(position);
            if (slot == null) {
                add(gap());
                continue;
            }
            SlotRef ref = refs.apply(SlotRef.of(slot));
            SlotView view = Optional.ofNullable(equipment.get(slot))
                    .map(item -> new SlotView(ref, item, icons, listener))
                    .orElseGet(() -> new SlotView(ref, icons, listener));
            if (view.item().isEmpty()) {
                artwork.load(slot, view::showGlyph);
            }
            slots.add(view);
            add(view);
        }
    }

    List<SlotView> slots() {
        return List.copyOf(slots);
    }

    Optional<SlotView> slotFor(EquipmentSlot slot) {
        return slots.stream()
                .filter(view -> view.ref().equals(refs.apply(SlotRef.of(slot))))
                .findFirst();
    }

    private static JPanel gap() {
        JPanel filler = new JPanel();
        filler.setBackground(ColorScheme.DARK_GRAY_COLOR);
        filler.setPreferredSize(new Dimension(SlotView.SIZE, SlotView.SIZE));
        return filler;
    }
}
