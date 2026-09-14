package dev.dutchy.runelite.gear.ui;

import dev.dutchy.runelite.gear.ColourLabel;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/** A right-click menu drawn like the rest of the panel: a dark card with flat, icon-led rows. */
// A menu's rows stay general even where the one caller that builds them pins an argument.
@SuppressWarnings("SameParameterValue")
final class ContextMenu extends JPopupMenu {

    private static final int ROW_HEIGHT = 24;
    private static final int INSET = 8;
    private static final int ICON_GAP = 8;
    private static final int ICON_WIDTH = ActionIcon.ADD.getIconWidth();
    private static final int ARROW_WIDTH = 10;

    private final List<JMenuItem> entries = new ArrayList<>();
    private final List<Submenu> submenus = new ArrayList<>();
    private final List<ColourSwatch> swatches = new ArrayList<>();

    ContextMenu() {
        style(this);
    }

    ContextMenu item(ActionIcon icon, String text, Runnable action) {
        return add(new Entry(icon, text, action, false, false));
    }

    /** A destructive choice, coloured so it is not picked by accident. */
    ContextMenu danger(String text, Runnable action) {
        return add(new Entry(ActionIcon.TRASH, text, action, true, false));
    }

    ContextMenu divider() {
        add(new Divider());
        return this;
    }

    /** A row that opens a side menu on hover; {@code fill} adds its entries. */
    ContextMenu submenu(ActionIcon icon, String text, Consumer<Submenu> fill) {
        Submenu submenu = new Submenu(icon, text);
        Objects.requireNonNull(fill, "fill").accept(submenu);
        submenus.add(submenu);
        entries.add(submenu);
        super.add(submenu);
        return this;
    }

    /** A row of colour dots; picking one closes the menu. */
    ContextMenu swatches(ColourLabel current, Consumer<ColourLabel> onPick) {
        Objects.requireNonNull(onPick, "onPick");
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        row.setOpaque(false);
        row.setBorder(BorderFactory.createEmptyBorder(3, INSET, 3, INSET));
        for (ColourLabel label : ColourLabel.values()) {
            ColourSwatch swatch = new ColourSwatch(label, label == current, () -> {
                setVisible(false);
                onPick.accept(label);
            });
            swatches.add(swatch);
            row.add(swatch);
        }
        super.add(row);
        return this;
    }

    List<ColourLabel> swatchLabels() {
        return swatches.stream().map(ColourSwatch::label).collect(Collectors.toList());
    }

    void pickSwatch(ColourLabel label) {
        swatches.stream().filter(swatch -> swatch.label() == label).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No swatch for " + label)).pick();
    }

    List<String> entryTexts() {
        return entries.stream().map(JMenuItem::getText).collect(Collectors.toList());
    }

    void invoke(String text) {
        entries.stream()
                .filter(entry -> entry.getText().equals(text))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No entry named " + text))
                .doClick(0);
    }

    Optional<Submenu> submenu(String text) {
        return submenus.stream().filter(submenu -> submenu.getText().equals(text)).findFirst();
    }

    private ContextMenu add(Entry entry) {
        entries.add(entry);
        super.add(entry);
        return this;
    }

    private static void style(JPopupMenu menu) {
        menu.setOpaque(true);
        menu.setBackground(Ui.SURFACE);
        menu.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Ui.OUTLINE, 1),
                BorderFactory.createEmptyBorder(4, 4, 4, 4)));
    }

    private static void paintRow(Graphics g, JMenuItem row, ActionIcon icon, boolean armed, Color hover, Color foreground, boolean arrow) {
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            if (armed) {
                g2.setColor(hover);
                g2.fillRoundRect(0, 0, row.getWidth(), row.getHeight(), Ui.RADIUS, Ui.RADIUS);
            }
            g2.setColor(foreground);
            if (icon != null) {
                icon.paintIcon(row, g2, INSET, (row.getHeight() - icon.getIconHeight()) / 2);
            }
            g2.setFont(row.getFont());
            FontMetrics metrics = g2.getFontMetrics();
            int y = (row.getHeight() - metrics.getHeight()) / 2 + metrics.getAscent();
            g2.drawString(row.getText(), INSET + ICON_WIDTH + ICON_GAP, y);
            if (arrow) {
                int tip = row.getWidth() - INSET - 2;
                int middle = row.getHeight() / 2;
                g2.drawLine(tip - 4, middle - 4, tip, middle);
                g2.drawLine(tip, middle, tip - 4, middle + 4);
            }
        } finally {
            g2.dispose();
        }
    }

    private static int rowWidth(JMenuItem row, boolean arrow) {
        FontMetrics metrics = row.getFontMetrics(row.getFont());
        return INSET + ICON_WIDTH + ICON_GAP + metrics.stringWidth(row.getText()) + INSET * 2 + (arrow ? ARROW_WIDTH : 0);
    }

    /** The side menu a submenu row opens: the same flat rows, one of which may be marked as the current choice. */
    static final class Submenu extends JMenu {

        private final ActionIcon icon;
        private final List<Entry> entries = new ArrayList<>();

        private Submenu(ActionIcon icon, String text) {
            super(Objects.requireNonNull(text, "text"));
            this.icon = Objects.requireNonNull(icon, "icon");
            setFont(FontManager.getRunescapeSmallFont());
            setOpaque(false);
            setBorderPainted(false);
            style(getPopupMenu());
        }

        void item(ActionIcon entryIcon, String text, Runnable action) {
            add(new Entry(entryIcon, text, action, false, false));
        }

        /** A choice that is the current one, marked with a tick. */
        void chosen(String text, Runnable action) {
            add(new Entry(ActionIcon.CHECK, text, action, false, true));
        }

        /** A choice that is not the current one, indented to line up with the ticked one. */
        void choice(String text, Runnable action) {
            add(new Entry(null, text, action, false, false));
        }

        void divider() {
            super.add(new Divider());
        }

        List<String> entryTexts() {
            return entries.stream().map(Entry::getText).collect(Collectors.toList());
        }

        List<String> chosenTexts() {
            return entries.stream().filter(entry -> entry.chosen).map(Entry::getText).collect(Collectors.toList());
        }

        void invoke(String text) {
            entries.stream()
                    .filter(entry -> entry.getText().equals(text))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("No entry named " + text))
                    .doClick(0);
        }

        private void add(Entry entry) {
            entries.add(entry);
            super.add(entry);
        }

        @Override
        public Color getForeground() {
            return isSelected() || isArmed() ? ColorScheme.BRAND_ORANGE : ColorScheme.LIGHT_GRAY_COLOR;
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(rowWidth(this, true), ROW_HEIGHT);
        }

        @Override
        protected void paintComponent(Graphics g) {
            paintRow(g, this, icon, isSelected() || isArmed(), Ui.SURFACE_HOVER, getForeground(), true);
        }
    }

    private static final class Entry extends JMenuItem {

        private final ActionIcon icon;
        private final boolean danger;
        private final boolean chosen;

        Entry(ActionIcon icon, String text, Runnable action, boolean danger, boolean chosen) {
            super(Objects.requireNonNull(text, "text"));
            this.icon = icon;
            this.danger = danger;
            this.chosen = chosen;
            Objects.requireNonNull(action, "action");
            setFont(FontManager.getRunescapeSmallFont());
            setOpaque(false);
            setBorderPainted(false);
            addActionListener(e -> action.run());
        }

        @Override
        public Color getForeground() {
            if (danger) {
                return isArmed() ? ColorScheme.TEXT_COLOR : Ui.DANGER;
            }
            if (chosen) {
                return isArmed() ? ColorScheme.TEXT_COLOR : ColorScheme.BRAND_ORANGE;
            }
            return isArmed() ? ColorScheme.BRAND_ORANGE : ColorScheme.LIGHT_GRAY_COLOR;
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(rowWidth(this, false), ROW_HEIGHT);
        }

        @Override
        protected void paintComponent(Graphics g) {
            paintRow(g, this, icon, isArmed(), danger ? Highlights.danger(Ui.SURFACE) : Ui.SURFACE_HOVER, getForeground(), false);
        }
    }

    private static final class Divider extends JComponent {

        Divider() {
            setPreferredSize(new Dimension(1, 7));
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            g.setColor(Ui.OUTLINE);
            g.fillRect(INSET / 2, getHeight() / 2, getWidth() - INSET, 1);
        }
    }
}
