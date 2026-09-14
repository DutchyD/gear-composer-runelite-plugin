package dev.dutchy.runelite.gear.ui;

import dev.dutchy.runelite.gear.GearSetup;
import dev.dutchy.runelite.gear.Owner;
import dev.dutchy.runelite.gear.content.SetupContentEditor;
import dev.dutchy.runelite.gear.content.SetupVariant;
import dev.dutchy.runelite.gear.guide.HelpTopic;
import dev.dutchy.runelite.libs.ui.icon.ItemIcon;
import dev.dutchy.runelite.libs.ui.icon.ItemIconFactory;
import dev.dutchy.runelite.libs.ui.item.ItemReference;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

final class SetupTile extends Card {

    static final String DUPLICATE = "Duplicate";
    static final String COPY_SHARE_CODE = "Copy share code";
    static final String COMPARE = "Compare…";
    static final String SHARE = "Share…";
    static final String PIN = "Pin";
    static final String UNPIN = "Unpin";
    static final String SET_HOTKEY = "Set hotkey…";
    static final String SHARE_WITH_ALL = "Share with all accounts";
    static final String VARIANTS = "Variants";
    static final String NEW_VARIANT = "New variant…";

    static String moveTo(String accountName) {
        return "Move to " + accountName;
    }
    static final int WIDTH = 46;
    static final int HEIGHT = 54;
    private static final int ICON_SIZE = 32;
    private static final int LABEL_BAR = 3;
    private static final int DELETE_MARK_SIZE = 18;
    private static final float DELETE_MARK_STROKE = 3f;

    static final int COMPACT_WIDTH = 32;
    static final int COMPACT_HEIGHT = 34;
    static final int LIST_HEIGHT = 30;
    private static final int COMPACT_ICON = 26;
    private static final int LIST_ICON = 22;

    private final GearSetup setup;
    private final SetupActions actions;
    private final BulkSelector bulk;
    private final BulkTarget target;
    private final TileStyle style;
    private final HoverPreview preview;
    private boolean hovered;
    private boolean selected;

    SetupTile(GearSetup setup, ItemIconFactory icons, SetupActions actions, SetupDragController drags, BulkSelector bulk) {
        this(setup, icons, actions, drags, bulk, TileStyle.GRID, HoverPreview.none());
    }

    SetupTile(GearSetup setup, ItemIconFactory icons, SetupActions actions, SetupDragController drags, BulkSelector bulk,
              TileStyle style, HoverPreview preview) {
        this.setup = Objects.requireNonNull(setup, "setup");
        this.actions = Objects.requireNonNull(actions, "actions");
        this.bulk = bulk;
        this.target = BulkTarget.of(setup.id());
        this.style = Objects.requireNonNull(style, "style");
        this.preview = Objects.requireNonNull(preview, "preview");
        Objects.requireNonNull(icons, "icons");

        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setToolTipText(shownName() + " - click to show it in the bank");
        Help.anchor(this, HelpTopic.SETUP_TILE);
        layOut(icons);

        installMouseHandling(drags, bulk);
        refreshBackground();
    }

    TileStyle style() {
        return style;
    }

    private void layOut(ItemIconFactory icons) {
        switch (style) {
            case COMPACT:
                setLayout(new BorderLayout());
                setPreferredSize(new Dimension(COMPACT_WIDTH, COMPACT_HEIGHT));
                setMinimumSize(new Dimension(COMPACT_WIDTH, COMPACT_HEIGHT));
                setBorder(BorderFactory.createEmptyBorder(3, 2, 3, 2));
                add(iconComponent(icons, COMPACT_ICON), BorderLayout.CENTER);
                break;
            case LIST:
                setLayout(new BorderLayout(6, 0));
                setPreferredSize(new Dimension(0, LIST_HEIGHT));
                setMinimumSize(new Dimension(0, LIST_HEIGHT));
                setBorder(BorderFactory.createEmptyBorder(3, 6, 3, 8));
                add(iconComponent(icons, LIST_ICON), BorderLayout.WEST);
                add(fullNameLabel(), BorderLayout.CENTER);
                add(detailLabel(), BorderLayout.EAST);
                break;
            default:
                setLayout(new BorderLayout(0, 2));
                setPreferredSize(new Dimension(WIDTH, HEIGHT));
                setMinimumSize(new Dimension(WIDTH, HEIGHT));
                setBorder(BorderFactory.createEmptyBorder(4, 2, 3, 2));
                add(iconComponent(icons, ICON_SIZE), BorderLayout.CENTER);
                add(nameLabel(), BorderLayout.SOUTH);
                break;
        }
    }

    private JLabel fullNameLabel() {
        JLabel label = new JLabel(setup.name());
        label.setFont(FontManager.getRunescapeSmallFont());
        label.setForeground(ColorScheme.TEXT_COLOR);
        return label;
    }

    /** The chosen variant, item count, and the hotkey when there is one, for the list style. */
    private JLabel detailLabel() {
        List<String> parts = new ArrayList<>();
        if (setup.hasVariants()) {
            parts.add(setup.variant().name());
        }
        if (setup.meta().hasHotkey()) {
            parts.add(setup.meta().hotkey().describe());
        }
        parts.add(String.valueOf(SetupContentEditor.allItems(setup.content()).size()));
        return Ui.hint(String.join(" · ", parts));
    }

    /** The name with the chosen variant after it when there is more than one. */
    private String shownName() {
        return setup.hasVariants() ? setup.name() + " (" + setup.variant().name() + ")" : setup.name();
    }

    GearSetup setup() {
        return setup;
    }

    boolean isSelected() {
        return selected;
    }

    void setSelected(boolean isSelected) {
        selected = isSelected;
        refreshBackground();
    }

    void refreshBulkHighlight() {
        refreshBackground();
    }

    /** Image of this tile, used as the drag preview. */
    BufferedImage snapshot() {
        BufferedImage image = new BufferedImage(Math.max(1, getWidth()), Math.max(1, getHeight()),
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        try {
            print(g);
        } finally {
            g.dispose();
        }
        return image;
    }

    private void paintLabelBar(Graphics g) {
        if (setup.meta().label().isNone()) {
            return;
        }
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(setup.meta().label().rgb()));
            g2.fillRoundRect(1, 1, getWidth() - 2, LABEL_BAR + 2, 4, 4);
            g2.fillRect(1, LABEL_BAR, getWidth() - 2, 1);
        } finally {
            g2.dispose();
        }
    }

    private void paintKeyBadge(Graphics g) {
        if (!setup.meta().hasHotkey() || style == TileStyle.LIST) {
            return;
        }
        String text = setup.meta().hotkey().describe();
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setFont(FontManager.getRunescapeSmallFont());
            FontMetrics metrics = g2.getFontMetrics();
            int width = metrics.stringWidth(text) + 6;
            int height = metrics.getAscent() + 3;
            g2.setColor(ColorScheme.DARK_GRAY_COLOR);
            g2.fillRoundRect(2, 2 + (setup.meta().label().isNone() ? 0 : LABEL_BAR), width, height, 4, 4);
            g2.setColor(ColorScheme.LIGHT_GRAY_COLOR);
            g2.drawString(text, 5, 2 + (setup.meta().label().isNone() ? 0 : LABEL_BAR) + metrics.getAscent());
        } finally {
            g2.dispose();
        }
    }

    private JComponent iconComponent(ItemIconFactory icons, int size) {
        if (setup.icon().isPresent()) {
            ItemIcon icon = icons.icon(size, size);
            icon.setItem(ItemReference.byId(setup.icon().get()));
            return icon;
        }
        return placeholder(size);
    }

    private JComponent placeholder(int size) {
        JLabel label = new JLabel(setup.name().substring(0, 1).toUpperCase(Locale.ROOT), SwingConstants.CENTER);
        label.setFont(FontManager.getRunescapeBoldFont());
        label.setForeground(ColorScheme.BRAND_ORANGE);
        label.setPreferredSize(new Dimension(size, size));
        return label;
    }

    private JLabel nameLabel() {
        JLabel label = new JLabel(setup.name(), SwingConstants.CENTER);
        label.setFont(FontManager.getRunescapeSmallFont());
        label.setForeground(ColorScheme.TEXT_COLOR);
        label.setText(truncate(setup.name(), label.getFont()));
        return label;
    }

    private String truncate(String text, Font font) {
        int maxWidth = WIDTH - 4;
        FontMetrics metrics = getFontMetrics(font);
        if (metrics.stringWidth(text) <= maxWidth) {
            return text;
        }
        String ellipsis = "…";
        int available = maxWidth - metrics.stringWidth(ellipsis);
        StringBuilder shortened = new StringBuilder();
        for (char c : text.toCharArray()) {
            if (metrics.stringWidth(shortened.toString() + c) > available) {
                break;
            }
            shortened.append(c);
        }
        return shortened + ellipsis;
    }

    private void installMouseHandling(SetupDragController drags, BulkSelector bulk) {
        if (bulk != null) {
            installBulkHandling(bulk);
            return;
        }
        MouseAdapter adapter = new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                hovered = true;
                preview.arm(SetupTile.this, setup);
                refreshBackground();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hovered = false;
                preview.hide();
                refreshBackground();
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    actions.activate(setup);
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                preview.hide();
                if (maybeShowMenu(e)) {
                    return;
                }
                if (drags != null) {
                    drags.press(SetupTile.this, e);
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (drags != null) {
                    drags.drag(SetupTile.this, e);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (maybeShowMenu(e)) {
                    if (drags != null) {
                        drags.cancel();
                    }
                    return;
                }
                if (drags != null) {
                    drags.release(SetupTile.this, e);
                }
            }
        };
        addMouseListener(adapter);
        addMouseMotionListener(adapter);
    }

    private void installBulkHandling(BulkSelector bulk) {
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
            public void mouseClicked(MouseEvent e) {
                bulk.select(target, e.isShiftDown());
            }
        });
    }

    private boolean maybeShowMenu(MouseEvent e) {
        if (!e.isPopupTrigger()) {
            return false;
        }
        contextMenu().show(this, e.getX(), e.getY());
        return true;
    }

    ContextMenu contextMenu() {
        ContextMenu menu = new ContextMenu()
                .item(ActionIcon.GRID, "Edit contents", () -> actions.editContents(setup))
                .submenu(ActionIcon.COPY, VARIANTS, this::fillVariants)
                .item(ActionIcon.COPY, DUPLICATE, () -> actions.duplicate(setup))
                .item(ActionIcon.RENAME, "Rename", () -> actions.edit(setup))
                .item(ActionIcon.PIN, setup.isPinned() ? UNPIN : PIN, () -> actions.togglePin(setup))
                .item(ActionIcon.KEY, setup.meta().hasHotkey() ? "Hotkey: " + setup.meta().hotkey().describe() : SET_HOTKEY, () -> actions.chooseHotkey(setup))
                .item(ActionIcon.COPY, COPY_SHARE_CODE, () -> actions.copyShareCode(setup))
                .item(ActionIcon.GRID, COMPARE, () -> actions.compare(setup))
                .item(ActionIcon.COPY, SHARE, () -> actions.share(setup))
                .swatches(setup.meta().label(), label -> actions.setLabel(setup, label));
        for (Owner owner : actions.otherOwners(setup)) {
            menu.item(ActionIcon.SYNC, owner.isShared() ? SHARE_WITH_ALL : moveTo(actions.describeOwner(owner)), () -> actions.moveTo(setup, owner));
        }
        return menu.divider().danger("Delete", () -> actions.delete(setup));
    }

    private void fillVariants(ContextMenu.Submenu menu) {
        List<SetupVariant> variants = setup.variants();
        for (int i = 0; i < variants.size(); i++) {
            int index = i;
            String name = variants.get(i).name();
            if (i == setup.selectedIndex()) {
                menu.chosen(name, () -> actions.selectVariant(setup, index));
            } else {
                menu.choice(name, () -> actions.selectVariant(setup, index));
            }
        }
        if (variants.size() < GearSetup.MAX_VARIANTS) {
            menu.divider();
            menu.item(ActionIcon.ADD, NEW_VARIANT, () -> actions.addVariant(setup));
        }
    }

    boolean isMarkedForDeletion() {
        return bulk != null && bulk.isSelected(target);
    }

    private void refreshBackground() {
        setBackground(background());
        setOutline(isMarkedForDeletion() ? Ui.DANGER : selected ? ColorScheme.BRAND_ORANGE : Ui.OUTLINE);
        repaint();
    }

    private Color background() {
        if (isMarkedForDeletion()) {
            return Highlights.danger(Ui.SURFACE);
        }
        if (selected) {
            return Highlights.selection(Ui.SURFACE);
        }
        return hovered ? Ui.SURFACE_HOVER : Ui.SURFACE;
    }

    @Override
    protected void paintChildren(Graphics g) {
        super.paintChildren(g);
        paintLabelBar(g);
        paintKeyBadge(g);
        if (!isMarkedForDeletion()) {
            return;
        }
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setStroke(new BasicStroke(DELETE_MARK_STROKE, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            int centreX = style == TileStyle.LIST ? getInsets().left + LIST_ICON / 2 : getWidth() / 2;
            int centreY = style == TileStyle.LIST ? getHeight() / 2 : getInsets().top + (style == TileStyle.COMPACT ? COMPACT_ICON : ICON_SIZE) / 2;
            int reach = DELETE_MARK_SIZE / 2;
            g2.setColor(Color.BLACK);
            g2.drawLine(centreX - reach + 1, centreY - reach + 1, centreX + reach + 1, centreY + reach + 1);
            g2.drawLine(centreX + reach + 1, centreY - reach + 1, centreX - reach + 1, centreY + reach + 1);
            g2.setColor(Ui.DANGER);
            g2.drawLine(centreX - reach, centreY - reach, centreX + reach, centreY + reach);
            g2.drawLine(centreX + reach, centreY - reach, centreX - reach, centreY + reach);
        } finally {
            g2.dispose();
        }
    }
}
