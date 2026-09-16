package dev.dutchy.runelite.gear.ui;

import dev.dutchy.runelite.gear.content.*;
import dev.dutchy.runelite.gear.guide.HelpTopic;
import dev.dutchy.runelite.libs.ui.icon.ItemIconFactory;
import net.runelite.client.ui.ColorScheme;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Objects;

/** One cell of a custom layout on the contents page: a heading with its menu, then its slots or an offer to give it a kind. */
final class CellView extends JPanel {

    static final String FILL_FROM_SETUP = "Fill from setup…";
    static final String FILL_FROM_GAME = "Fill from game";
    static final String RENAME = "Rename…";
    static final String CLEAR_ITEMS = "Clear items";
    static final String EMPTY_CELL = "Empty this cell";
    private static final int PLACEHOLDER_HEIGHT = 64;

    private final CellRef ref;
    private final LayoutCell cell;
    private final CellActions actions;
    private final List<SlotView> slots;

    CellView(CellRef ref, LayoutCell cell, ItemIconFactory icons, EquipmentSlotArtwork artwork, SlotView.Listener listener, CellActions actions) {
        this.ref = Objects.requireNonNull(ref, "ref");
        this.cell = Objects.requireNonNull(cell, "cell");
        this.actions = Objects.requireNonNull(actions, "actions");
        Objects.requireNonNull(icons, "icons");
        Objects.requireNonNull(listener, "listener");

        setLayout(new BorderLayout(0, Ui.SMALL_GAP));
        setBackground(ColorScheme.DARK_GRAY_COLOR);
        setAlignmentX(Component.LEFT_ALIGNMENT);
        setBorder(BorderFactory.createEmptyBorder(0, 0, Ui.GAP, 0));

        JComponent body;
        switch (cell.kind()) {
            case EQUIPMENT:
                EquipmentView equipment = new EquipmentView(cell.equipment(), icons, artwork, listener, inner -> SlotRef.in(ref, inner));
                slots = equipment.slots();
                body = equipment;
                break;
            case INVENTORY:
                ItemGridView grid = new ItemGridView(GridKind.INVENTORY, cell.inventory(), icons, listener, inner -> SlotRef.in(ref, inner));
                slots = grid.slots();
                body = grid;
                break;
            default:
                slots = List.of();
                body = placeholder();
                break;
        }
        add(heading(), BorderLayout.NORTH);
        add(ContentBody.centered(body), BorderLayout.CENTER);
    }

    CellRef ref() {
        return ref;
    }

    List<SlotView> slots() {
        return slots;
    }

    ContextMenu contextMenu() {
        ContextMenu menu = new ContextMenu()
                .item(ActionIcon.COPY, FILL_FROM_SETUP, () -> actions.fillCellFromSetup(ref))
                .item(ActionIcon.SYNC, FILL_FROM_GAME, () -> actions.fillCellFromGame(ref))
                .item(ActionIcon.RENAME, RENAME, () -> actions.renameCell(ref))
                .divider()
                .item(ActionIcon.DELETE, CLEAR_ITEMS, () -> actions.clearCell(ref));
        return menu.danger(EMPTY_CELL, () -> actions.emptyCell(ref));
    }

    private JPanel heading() {
        JPanel row = Ui.panel(new BorderLayout(Ui.SMALL_GAP, 0));
        row.setBorder(BorderFactory.createEmptyBorder(0, 2, 2, 0));
        JLabel title = Ui.caption(cell.label());
        JLabel where = Ui.hint(ref.describe() + (cell.isBlank() ? "" : " · " + cell.kind().displayName()));
        JPanel text = Ui.panel(new FlowLayout(FlowLayout.LEFT, Ui.SMALL_GAP, 0));
        text.add(title);
        text.add(where);
        row.add(text, BorderLayout.CENTER);
        if (!cell.isBlank()) {
            FlatButton more = Help.describe(new FlatButton(ActionIcon.MORE, "More for " + cell.label(), () -> {
            }), HelpTopic.CELL_MENU);
            more.addActionListener(e -> contextMenu().show(more, 0, more.getHeight()));
            row.add(more, BorderLayout.EAST);
        }
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, row.getPreferredSize().height));
        return row;
    }

    private JComponent placeholder() {
        Card card = new Card();
        card.setLayout(new BorderLayout(0, Ui.SMALL_GAP));
        card.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        card.setPreferredSize(new Dimension(SlotView.SIZE * 4 + 6, PLACEHOLDER_HEIGHT));
        card.setOutline(ColorScheme.BORDER_COLOR);
        JPanel choices = Ui.panel(new GridLayout(1, 2, Ui.SMALL_GAP, 0));
        choices.add(new Chip(CellKind.EQUIPMENT.displayName(), "Put an equipment set here", () -> actions.setCellKind(ref, CellKind.EQUIPMENT)));
        choices.add(new Chip(CellKind.INVENTORY.displayName(), "Put an inventory here", () -> actions.setCellKind(ref, CellKind.INVENTORY)));
        card.add(Ui.hint("Nothing here yet. Make it an"), BorderLayout.NORTH);
        card.add(choices, BorderLayout.CENTER);
        return card;
    }

}
