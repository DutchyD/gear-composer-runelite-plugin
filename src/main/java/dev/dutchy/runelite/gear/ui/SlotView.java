package dev.dutchy.runelite.gear.ui;

import dev.dutchy.runelite.gear.content.*;
import dev.dutchy.runelite.libs.ui.icon.ItemIcon;
import dev.dutchy.runelite.libs.ui.icon.ItemIconFactory;
import dev.dutchy.runelite.libs.ui.image.ImageTransforms;
import dev.dutchy.runelite.libs.ui.item.ItemReference;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

final class SlotView extends Card {

    /** How the host hears about clicks, drags, and menu choices on a slot. */
    interface Listener {

        void clicked(SlotView view, MouseEvent event);

        void pressed(SlotView view, MouseEvent event);

        void dragged(SlotView view, MouseEvent event);

        void released(SlotView view, MouseEvent event);

        void edit(SlotRef ref);

        void clear(SlotRef ref);

        void fillRemaining(SlotRef from);

        void fillRow(SlotRef from);

        /** Opens the divider over the slot's column for editing, or starts one there. */
        void editDivider(SlotRef slot);

        /** Drops the divider over the slot's column. */
        void removeDivider(SlotRef slot);

        /** Applies a one-step change to the divider over the slot's column. */
        void changeDivider(SlotRef slot, DividerChange change);
    }

    enum DropState { NONE, ALLOWED, REFUSED }

    static final int SIZE = 44;
    static final String ADD_ITEM = "Add item";
    static final String CHANGE_ITEM = "Change item";
    static final String DELETE_ITEM = "Delete item";
    static final String FILL_REMAINING = "Fill remaining";
    static final String FILL_ROW = "Fill this row";
    static final String ADD_DIVIDER = "Divider above…";
    private static final int ICON_SIZE = 34;
    private static final int QUANTITY_INSET = 3;
    private static final float GHOST_OPACITY = 0.3f;
    private static final float GLYPH_OPACITY = 0.45f;
    private static final Color NOTE_BADGE = new Color(120, 190, 255);

    private final SlotRef ref;
    private final SetupItem item;
    private final SetupItem shadow;
    private final Listener listener;
    private boolean hovered;
    private boolean selected;
    private DropState dropState = DropState.NONE;
    private BufferedImage glyph;

    /** An empty slot. */
    SlotView(SlotRef ref, ItemIconFactory icons, Listener listener) {
        this(ref, null, null, icons, listener);
    }

    /** A slot holding an item. */
    SlotView(SlotRef ref, SetupItem item, ItemIconFactory icons, Listener listener) {
        this(ref, Objects.requireNonNull(item, "item"), null, icons, listener);
    }

    private SlotView(SlotRef ref, SetupItem item, SetupItem shadow, ItemIconFactory icons, Listener listener) {
        this.ref = Objects.requireNonNull(ref, "ref");
        this.item = item;
        this.shadow = shadow;
        this.listener = Objects.requireNonNull(listener, "listener");

        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(SIZE, SIZE));
        setMinimumSize(new Dimension(SIZE, SIZE));
        setMaximumSize(new Dimension(SIZE, SIZE));
        setRadius(4);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setToolTipText(tooltip(null));

        if (item != null) {
            ItemIcon icon = icons.icon(ICON_SIZE, ICON_SIZE);
            icon.setResolvedListener(resolved -> setToolTipText(tooltip(resolved.name())));
            icon.setItem(ItemReference.byId(item.id()));
            add(icon, BorderLayout.CENTER);
        } else if (shadow != null) {
            ItemIcon icon = icons.icon(ICON_SIZE, ICON_SIZE);
            icon.setImageTransform(ImageTransforms.opacity(GHOST_OPACITY));
            icon.setResolvedListener(resolved -> setToolTipText(tooltip(resolved.name())));
            icon.setItem(ItemReference.byId(shadow.id()));
            add(icon, BorderLayout.CENTER);
        }

        MouseAdapter mouse = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    listener.clicked(SlotView.this, e);
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (maybeShowMenu(e)) {
                    return;
                }
                listener.pressed(SlotView.this, e);
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                listener.dragged(SlotView.this, e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (maybeShowMenu(e)) {
                    return;
                }
                listener.released(SlotView.this, e);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                hovered = true;
                refresh();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hovered = false;
                refresh();
            }
        };
        addMouseListener(mouse);
        addMouseMotionListener(mouse);
        refresh();
    }

    SlotRef ref() {
        return ref;
    }

    Optional<SetupItem> item() {
        return Optional.ofNullable(item);
    }

    void setSelected(boolean isSelected) {
        selected = isSelected;
        refresh();
    }

    DropState dropState() {
        return dropState;
    }

    /** The game's own picture of what goes here, drawn faded while the slot is empty. */
    void showGlyph(BufferedImage image) {
        glyph = Objects.requireNonNull(image, "image");
        repaint();
    }

    boolean hasGlyph() {
        return glyph != null;
    }

    void setDropState(DropState state) {
        dropState = Objects.requireNonNull(state, "state");
        refresh();
    }

    ContextMenu contextMenu() {
        ContextMenu menu = new ContextMenu();
        if (item == null) {
            menu.item(ActionIcon.ADD, ADD_ITEM, () -> listener.edit(ref));
            if (ref.inGrid()) {
                menu.item(ActionIcon.RENAME, ADD_DIVIDER, () -> listener.editDivider(ref));
            }
            return menu;
        }
        menu.item(ActionIcon.RENAME, CHANGE_ITEM, () -> listener.edit(ref));
        if (ref.inGrid()) {
            menu.item(ActionIcon.MOVE_DOWN, FILL_REMAINING, () -> listener.fillRemaining(ref))
                    .item(ActionIcon.GRID, FILL_ROW, () -> listener.fillRow(ref))
                    .item(ActionIcon.RENAME, ADD_DIVIDER, () -> listener.editDivider(ref));
        }
        return menu.divider().danger(DELETE_ITEM, () -> listener.clear(ref));
    }

    private boolean maybeShowMenu(MouseEvent e) {
        if (!e.isPopupTrigger()) {
            return false;
        }
        contextMenu().show(this, e.getX(), e.getY());
        return true;
    }

    /** The item name on its own line once it has resolved, then the slot and how it is filled. */
    private String tooltip(String itemName) {
        String details;
        if (item == null) {
            details = shadow != null ? ref.describe() + ": same as base, click to swap" : "Set " + ref.describe();
        } else {
            String amount = item.quantity().isPresent() ? item.quantity().getAsInt() + "x" : "whatever the bank holds";
            String rule = item.match() == ItemMatch.DEFAULT ? "" : ", " + item.match().displayName().toLowerCase(Locale.ROOT);
            String standIns = item.hasAlternatives() ? ", " + item.alternatives().size() + " alternative(s)" : "";
            String note = item.noted() ? ", as a note" : "";
            details = ref.describe() + ": " + amount + rule + standIns + note;
        }
        return itemName == null ? details : "<html><b>" + escape(itemName) + "</b><br>" + escape(details) + "</html>";
    }

    private static String escape(String text) {
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }


    private void refresh() {
        setBackground(background());
        setOutline(outline());
        repaint();
    }

    private Color background() {
        switch (dropState) {
            case ALLOWED:
                return Highlights.selection(Ui.SURFACE);
            case REFUSED:
                return Highlights.danger(Ui.SURFACE);
            default:
                return selected ? Highlights.selection(Ui.SURFACE) : hovered ? Ui.SURFACE_HOVER : Ui.SURFACE;
        }
    }

    private Color outline() {
        switch (dropState) {
            case ALLOWED:
                return ColorScheme.BRAND_ORANGE;
            case REFUSED:
                return Ui.DANGER;
            default:
                return selected ? ColorScheme.BRAND_ORANGE
                        : hovered ? ColorScheme.LIGHT_GRAY_COLOR
                        : item != null ? Ui.OUTLINE : ColorScheme.BORDER_COLOR;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (glyph == null || item != null || shadow != null) {
            return;
        }
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, GLYPH_OPACITY));
            double scale = Math.min(1.0, (double) ICON_SIZE / Math.max(glyph.getWidth(), glyph.getHeight()));
            int width = (int) Math.round(glyph.getWidth() * scale);
            int height = (int) Math.round(glyph.getHeight() * scale);
            g2.drawImage(glyph, (getWidth() - width) / 2, (getHeight() - height) / 2, width, height, null);
        } finally {
            g2.dispose();
        }
    }

    @Override
    protected void paintChildren(Graphics g) {
        super.paintChildren(g);
        if (item != null && item.noted()) {
            g.setFont(FontManager.getRunescapeSmallFont());
            int x = getWidth() - g.getFontMetrics().stringWidth("N") - QUANTITY_INSET;
            g.setColor(Color.BLACK);
            g.drawString("N", x + 1, QUANTITY_INSET + g.getFontMetrics().getAscent() + 1);
            g.setColor(NOTE_BADGE);
            g.drawString("N", x, QUANTITY_INSET + g.getFontMetrics().getAscent());
        }
        if (item == null || !item.showsQuantity()) {
            return;
        }
        {
            g.setFont(FontManager.getRunescapeSmallFont());
            int amount = item.quantity().orElseThrow();
            String text = ItemQuantityFormat.text(amount);
            g.setColor(Color.BLACK);
            g.drawString(text, QUANTITY_INSET + 1, QUANTITY_INSET + g.getFontMetrics().getAscent() + 1);
            g.setColor(ItemQuantityFormat.color(amount));
            g.drawString(text, QUANTITY_INSET, QUANTITY_INSET + g.getFontMetrics().getAscent());
        }
    }
}
