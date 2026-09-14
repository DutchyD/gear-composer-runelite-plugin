package dev.dutchy.runelite.gear.ui;

import dev.dutchy.runelite.gear.content.SetupType;
import dev.dutchy.runelite.libs.ui.image.ImageTransforms;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.Objects;
import java.util.function.Consumer;

final class SetupTypeButton extends Card {

    static final int HEIGHT = 52;
    private static final int ART_SIZE = 36;

    private final SetupType type;
    private final JLabel art = new JLabel("", SwingConstants.CENTER);

    private boolean selected;
    private boolean hovered;

    SetupTypeButton(SetupType type, SetupTypeArtwork artwork, Consumer<SetupType> onChosen) {
        this(type, artwork, onChosen, true);
    }

    /** The layout of an existing setup: shown the same way, but not clickable. */
    static SetupTypeButton locked(SetupType type, SetupTypeArtwork artwork) {
        return new SetupTypeButton(type, artwork, chosen -> {
        }, false);
    }

    private SetupTypeButton(SetupType type, SetupTypeArtwork artwork, Consumer<SetupType> onChosen, boolean interactive) {
        this.type = Objects.requireNonNull(type, "type");
        Objects.requireNonNull(artwork, "artwork");
        Objects.requireNonNull(onChosen, "onChosen");

        setLayout(new BorderLayout(8, 0));
        setPreferredSize(new Dimension(0, HEIGHT));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, HEIGHT));
        setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
        setToolTipText(description());

        art.setPreferredSize(new Dimension(ART_SIZE + 4, ART_SIZE));

        JLabel title = new JLabel(type.displayName());
        title.setFont(FontManager.getRunescapeBoldFont());
        title.setForeground(ColorScheme.TEXT_COLOR);

        JLabel subtitle = new JLabel(description());
        subtitle.setFont(FontManager.getRunescapeSmallFont());
        subtitle.setForeground(ColorScheme.LIGHT_GRAY_COLOR);

        JPanel text = Ui.panel(new BorderLayout());
        text.add(title, BorderLayout.NORTH);
        text.add(subtitle, BorderLayout.CENTER);


        add(art, BorderLayout.WEST);
        add(text, BorderLayout.CENTER);

        if (interactive) {
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    onChosen.accept(type);
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
        }

        artwork.load(type, this::showArt);
        refresh();
    }

    SetupType type() {
        return type;
    }

    boolean isChosen() {
        return selected;
    }

    void setChosen(boolean isChosen) {
        selected = isChosen;
        refresh();
    }

    private void showArt(BufferedImage image) {
        art.setIcon(new ImageIcon(ImageTransforms.scaleDownToFit(ART_SIZE, ART_SIZE).apply(image)));
    }

    private String description() {
        switch (type) {
            case GEAR:
                return "Equipment and inventory";
            case BANK:
                return "Both sides of the bank";
            default:
                return "Your own grid of cells";
        }
    }

    private void refresh() {
        setBackground(background());
        setOutline(selected ? ColorScheme.BRAND_ORANGE : Ui.OUTLINE);
        repaint();
    }

    private Color background() {
        if (selected) {
            return Highlights.selection(Ui.SURFACE);
        }
        return hovered ? Ui.SURFACE_HOVER : Ui.SURFACE;
    }
}
