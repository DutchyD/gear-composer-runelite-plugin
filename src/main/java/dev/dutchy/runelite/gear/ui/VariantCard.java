package dev.dutchy.runelite.gear.ui;

import dev.dutchy.runelite.gear.GearSetup;
import dev.dutchy.runelite.gear.content.SetupContentEditor;
import dev.dutchy.runelite.gear.content.SetupVariant;
import dev.dutchy.runelite.libs.ui.icon.ItemIconFactory;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Objects;

/** One variant in the list: a preview, its name and totals, lit when the page edits it and badged when the bank shows it; click to edit, right-click for more. */
final class VariantCard extends Card {

    static final String SHOW = "Show";
    static final String RENAME = "Rename…";
    static final String DUPLICATE = "Duplicate";
    static final String MOVE_UP = "Move up";
    static final String MOVE_DOWN = "Move down";
    static final String DELETE = "Delete";
    static final String SHOWN = "Shown";
    static final int CELL = 12;

    private final SetupVariant variant;
    private final int index;
    private final int count;
    private final boolean chosen;
    private final boolean shown;
    private final VariantActions actions;
    private final JLabel totals = Ui.hint("");
    private final FlatButton more;
    private boolean hovered;

    VariantCard(SetupVariant variant, int index, int count, boolean chosen, boolean shown, ItemIconFactory icons,
                VariantActions actions) {
        this.variant = Objects.requireNonNull(variant, "variant");
        this.index = index;
        this.count = count;
        this.chosen = chosen;
        this.shown = shown;
        this.actions = Objects.requireNonNull(actions, "actions");
        Objects.requireNonNull(icons, "icons");
        setLayout(new BorderLayout(Ui.SMALL_GAP, 0));
        setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 4));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setToolTipText(chosen ? "Editing " + variant.name() + "; right-click for more" : "Click to edit " + variant.name() + "; right-click for more");

        JLabel name = new JLabel(variant.name());
        name.setFont(FontManager.getRunescapeBoldFont());
        name.setForeground(chosen ? ColorScheme.BRAND_ORANGE : ColorScheme.TEXT_COLOR);
        JPanel text = Ui.column(2, name, totals);
        if (shown) {
            JLabel badge = Ui.hint(SHOWN);
            badge.setForeground(ColorScheme.BRAND_ORANGE);
            text.add(badge);
        }

        more = new FlatButton(ActionIcon.MORE, "Rename, duplicate, move or delete this variant", this::showMenu);
        more.setPreferredSize(new Dimension(20, 20));

        add(new MiniLayoutView(variant.content(), icons, CELL), BorderLayout.WEST);
        add(text, BorderLayout.CENTER);
        add(more, BorderLayout.EAST);
        installMouseHandling();
        refresh();
    }

    SetupVariant variant() {
        return variant;
    }

    /** Whether the page edits this variant. */
    boolean isChosen() {
        return chosen;
    }

    /** Whether the bank shows this variant. */
    boolean isShown() {
        return shown;
    }

    String totals() {
        return totals.getText();
    }

    void refresh() {
        int items = SetupContentEditor.allItems(variant.content()).size();
        totals.setText(items == 1 ? "1 item" : items + " items");
        refreshBackground();
    }

    ContextMenu contextMenu() {
        ContextMenu menu = new ContextMenu();
        if (!shown) {
            menu.item(ActionIcon.CHECK, SHOW, () -> actions.selectVariant(index));
        }
        menu.item(ActionIcon.RENAME, RENAME, () -> actions.renameVariant(index));
        if (count < GearSetup.MAX_VARIANTS) {
            menu.item(ActionIcon.COPY, DUPLICATE, () -> actions.duplicateVariant(index));
        }
        if (index > 0) {
            menu.item(ActionIcon.MOVE_UP, MOVE_UP, () -> actions.moveVariant(index, index - 1));
        }
        if (index < count - 1) {
            menu.item(ActionIcon.MOVE_DOWN, MOVE_DOWN, () -> actions.moveVariant(index, index + 1));
        }
        if (count > 1) {
            menu.divider().danger(DELETE, () -> actions.deleteVariant(index));
        }
        return menu;
    }

    private void installMouseHandling() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                hovered = true;
                refreshBackground();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hovered = false;
                refreshBackground();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                maybeShowMenu(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                maybeShowMenu(e);
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e) && !chosen) {
                    actions.editVariant(index);
                }
            }
        });
    }

    private void showMenu() {
        contextMenu().show(more, 0, more.getHeight());
    }

    private void maybeShowMenu(MouseEvent e) {
        if (e.isPopupTrigger()) {
            contextMenu().show(this, e.getX(), e.getY());
        }
    }

    private void refreshBackground() {
        setOutline(chosen ? ColorScheme.BRAND_ORANGE : Ui.OUTLINE);
        setBackground(chosen ? Highlights.selection(Ui.SURFACE) : hovered ? Ui.SURFACE_HOVER : Ui.SURFACE);
        repaint();
    }
}
