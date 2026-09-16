package dev.dutchy.runelite.libs.ui.selector;

import dev.dutchy.runelite.libs.ui.button.ItemButton;
import dev.dutchy.runelite.libs.ui.button.ItemButtonFactory;
import dev.dutchy.runelite.libs.ui.item.ItemReference;
import dev.dutchy.runelite.libs.ui.item.ResolvedItem;
import dev.dutchy.runelite.libs.ui.style.ItemButtonStyle;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Objects;
import java.util.function.Consumer;

final class ItemSearchResultRow extends JPanel {

    private static final Color IDLE = ColorScheme.DARKER_GRAY_COLOR;
    private static final Color HOVER = ColorScheme.DARKER_GRAY_HOVER_COLOR;
    private static final Color HIGHLIGHT = ColorScheme.MEDIUM_GRAY_COLOR;

    private final ResolvedItem item;
    private final ItemButton sprite;
    private boolean highlighted;
    private boolean hovered;

    ItemSearchResultRow(ResolvedItem item, ItemButtonFactory buttons, ItemButtonStyle spriteStyle,
                        Consumer<ResolvedItem> onSelect, ItemRowDragListener drag) {
        this.item = Objects.requireNonNull(item, "item");
        Objects.requireNonNull(onSelect, "onSelect");
        setLayout(new BorderLayout(6, 0));
        setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        sprite = buttons.button(ItemReference.byId(item.id()))
                .style(spriteStyle)
                .tooltip(item.name())
                .onClick(event -> onSelect.accept(item))
                .build();

        JLabel name = new JLabel(item.name());
        name.setFont(FontManager.getRunescapeSmallFont());
        name.setForeground(ColorScheme.TEXT_COLOR);

        JLabel id = new JLabel(item.id().toString());
        id.setFont(FontManager.getRunescapeSmallFont());
        id.setForeground(ColorScheme.LIGHT_GRAY_COLOR);

        add(sprite, BorderLayout.WEST);
        add(name, BorderLayout.CENTER);
        add(id, BorderLayout.EAST);

        MouseAdapter mouse = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                onSelect.accept(item);
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (drag != null) {
                    drag.pressed(item, e);
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (drag != null) {
                    drag.dragged(item, e);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (drag != null) {
                    drag.released(item, e);
                }
            }

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
        };
        addMouseListener(mouse);
        addMouseMotionListener(mouse);
        MouseAdapter spriteDrag = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (drag != null) {
                    drag.pressed(item, SwingUtilities.convertMouseEvent(sprite, e, ItemSearchResultRow.this));
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (drag != null) {
                    drag.dragged(item, SwingUtilities.convertMouseEvent(sprite, e, ItemSearchResultRow.this));
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (drag != null) {
                    drag.released(item, SwingUtilities.convertMouseEvent(sprite, e, ItemSearchResultRow.this));
                }
            }
        };
        sprite.addMouseListener(spriteDrag);
        sprite.addMouseMotionListener(spriteDrag);
        refreshBackground();
    }

    /** The sprite, which is where a drag most naturally starts. */
    JComponent sprite() {
        return sprite;
    }

    ResolvedItem item() {
        return item;
    }

    void setHighlighted(boolean isHighlighted) {
        highlighted = isHighlighted;
        refreshBackground();
    }

    private void refreshBackground() {
        setBackground(highlighted ? HIGHLIGHT : hovered ? HOVER : IDLE);
    }
}
