package dev.dutchy.runelite.gear.ui;

import dev.dutchy.runelite.gear.content.*;
import dev.dutchy.runelite.gear.guide.HelpTopic;
import dev.dutchy.runelite.libs.ui.icon.ItemIconFactory;
import net.runelite.client.ui.ColorScheme;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Optional;

/** The slots of one content shape on the contents page, shown one row at a time. */
abstract class ContentBody extends JPanel {

    ContentBody() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(ColorScheme.DARK_GRAY_COLOR);
        setAlignmentX(Component.LEFT_ALIGNMENT);
    }

    static ContentBody of(SetupContent content, ItemIconFactory icons, EquipmentSlotArtwork artwork, SlotView.Listener listener,
                          ContentActions actions, CellActions cellActions) {
        if (content instanceof GearContent) {
            return new GearBody((GearContent) content, icons, artwork, listener, actions);
        }
        if (content instanceof BankContent) {
            return new BankBody((BankContent) content, icons, listener, actions);
        }
        return new CustomBody((CustomContent) content, icons, artwork, listener, cellActions);
    }

    /** The slots on show now, in reading order. */
    abstract List<SlotView> slots();

    /** Brings the state's row and cell into view. */
    abstract void show(ContentViewState view);

    /** The state with this body's row and cell filled in. */
    abstract ContentViewState remember(ContentViewState view);

    /** The action that belongs beside the "click a slot" hint, when the shape syncs as a whole. */
    Optional<JComponent> toolbarAction() {
        return Optional.empty();
    }

    /** A click landed in the slot; bodies with a map mark where. */
    void clicked(SlotRef ref) {
    }

    /** Switches to the first row holding an empty or a filled cell, so a guide can point at one. */
    void showRowWithCell(boolean blank) {
    }

    List<CellView> cellViews() {
        return List.of();
    }

    static FlatButton syncButton(String text, String tooltip, Runnable action) {
        return Help.describe(FlatButton.outlined(ActionIcon.SYNC, text, tooltip, action), HelpTopic.SYNC);
    }

    static JPanel heading(String text, JComponent action) {
        JPanel row = Ui.panel(new BorderLayout(Ui.SMALL_GAP, 0));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setBorder(BorderFactory.createEmptyBorder(0, 2, 2, 0));
        row.add(Ui.caption(text), BorderLayout.CENTER);
        if (action != null) {
            row.add(action, BorderLayout.EAST);
        }
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, row.getPreferredSize().height));
        return row;
    }

    static JPanel centered(Component child) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 2));
        row.setBackground(ColorScheme.DARK_GRAY_COLOR);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.add(child);
        return row;
    }
}
