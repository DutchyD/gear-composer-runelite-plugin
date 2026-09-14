package dev.dutchy.runelite.gear.ui;

import dev.dutchy.runelite.gear.Prayer;
import dev.dutchy.runelite.libs.ui.image.ImageTransforms;
import net.runelite.client.ui.ColorScheme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.function.Consumer;

/** The prayer book as a grid of icons; clicking one toggles it, lit when chosen and dimmed otherwise. */
final class PrayerPicker extends JPanel {

    static final int TILE = 34;
    private static final int ICON = 26;
    private static final float DIMMED = 0.35f;

    private final Map<Prayer, Tile> tiles = new EnumMap<>(Prayer.class);
    private final Set<Prayer> chosen = EnumSet.noneOf(Prayer.class);
    private final Consumer<Set<Prayer>> onChange;

    PrayerPicker(Set<Prayer> initial, PrayerArtwork artwork, Consumer<Set<Prayer>> onChange) {
        Objects.requireNonNull(initial, "initial");
        Objects.requireNonNull(artwork, "artwork");
        this.onChange = Objects.requireNonNull(onChange, "onChange");
        chosen.addAll(initial);
        setLayout(new GridLayout(0, Prayer.COLUMNS, 2, 2));
        setBackground(ColorScheme.DARK_GRAY_COLOR);
        for (Prayer prayer : Prayer.values()) {
            Tile tile = new Tile(prayer, () -> toggle(prayer));
            tiles.put(prayer, tile);
            add(tile);
            artwork.load(prayer, tile::showIcon);
        }
        refresh();
    }

    Set<Prayer> chosen() {
        return chosen.isEmpty() ? Set.of() : Collections.unmodifiableSet(EnumSet.copyOf(chosen));
    }

    void toggle(Prayer prayer) {
        if (!chosen.remove(prayer)) {
            chosen.add(prayer);
        }
        refresh();
        onChange.accept(chosen());
    }

    @SuppressWarnings("SameParameterValue")
    boolean isChosen(Prayer prayer) {
        return chosen.contains(prayer);
    }

    private void refresh() {
        tiles.forEach((prayer, tile) -> tile.setChosen(chosen.contains(prayer)));
    }

    private static final class Tile extends Card {
        private final JLabel icon = new JLabel("", SwingConstants.CENTER);
        private BufferedImage image;
        private boolean chosen;
        private boolean hovered;

        Tile(Prayer prayer, Runnable onClick) {
            setLayout(new BorderLayout());
            setPreferredSize(new Dimension(TILE, TILE));
            setRadius(4);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setToolTipText(prayer.displayName());
            icon.setText(prayer.displayName().substring(0, 1));
            icon.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
            add(icon, BorderLayout.CENTER);
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    onClick.run();
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
            });
            refresh();
        }

        void showIcon(BufferedImage sprite) {
            image = ImageTransforms.scaleDownToFit(ICON, ICON).apply(sprite);
            icon.setText("");
            refresh();
        }

        void setChosen(boolean isChosen) {
            chosen = isChosen;
            refresh();
        }

        private void refresh() {
            if (image != null) {
                icon.setIcon(new ImageIcon(chosen ? image : ImageTransforms.opacity(DIMMED).apply(image)));
            }
            setBackground(chosen ? Highlights.selection(Ui.SURFACE) : hovered ? Ui.SURFACE_HOVER : Ui.SURFACE);
            setOutline(chosen ? ColorScheme.BRAND_ORANGE : Ui.OUTLINE);
            repaint();
        }
    }
}
