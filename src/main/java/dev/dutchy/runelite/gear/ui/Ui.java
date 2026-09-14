package dev.dutchy.runelite.gear.ui;

import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;

import javax.swing.*;
import java.awt.*;

/** Spacing, colours, and small helpers shared by every page of the panel. */
final class Ui {

    static final int GAP = 10;
    static final int SMALL_GAP = 4;
    static final int RADIUS = 6;
    static final int FIELD_HEIGHT = 30;
    static final Color OUTLINE = new Color(52, 52, 52);
    static final Color DANGER = new Color(220, 62, 48);
    static final Color SURFACE = ColorScheme.DARKER_GRAY_COLOR;
    static final Color SURFACE_HOVER = ColorScheme.DARKER_GRAY_HOVER_COLOR;

    private Ui() {
    }

    static JPanel panel(LayoutManager layout) {
        JPanel panel = new JPanel(layout);
        panel.setOpaque(false);
        return panel;
    }

    /** Stacks children top to bottom at full width, separated by {@code gap} pixels. */
    static JPanel column(int gap, Component... children) {
        JPanel column = new JPanel();
        column.setLayout(new BoxLayout(column, BoxLayout.Y_AXIS));
        column.setOpaque(false);
        for (int i = 0; i < children.length; i++) {
            if (i > 0 && gap > 0) {
                column.add(Box.createVerticalStrut(gap));
            }
            if (children[i] instanceof JComponent) {
                ((JComponent) children[i]).setAlignmentX(Component.LEFT_ALIGNMENT);
            }
            column.add(children[i]);
        }
        return column;
    }

    /** A caption above a field, the way every form on the panel labels its inputs. */
    static JPanel labelled(String caption, JComponent field) {
        return labelled(caption, null, field);
    }

    static JPanel labelled(String caption, String trailingHint, JComponent field) {
        JPanel row = panel(new BorderLayout());
        row.add(caption(caption), BorderLayout.WEST);
        if (trailingHint != null) {
            row.add(hint(trailingHint), BorderLayout.EAST);
        }
        row.setBorder(BorderFactory.createEmptyBorder(0, 1, 3, 1));

        JPanel block = panel(new BorderLayout());
        block.add(row, BorderLayout.NORTH);
        block.add(field, BorderLayout.CENTER);
        return block;
    }

    static JLabel caption(String text) {
        JLabel label = new JLabel(text);
        label.setFont(FontManager.getRunescapeSmallFont());
        label.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
        return label;
    }

    static JLabel body(String text) {
        JLabel label = new JLabel(text);
        label.setFont(FontManager.getRunescapeSmallFont());
        label.setForeground(ColorScheme.TEXT_COLOR);
        return label;
    }

    /** Explanatory text in the body colour; wraps rather than truncates. */
    static WrappedText paragraph(String text) {
        return new WrappedText(text, FontManager.getRunescapeSmallFont(), ColorScheme.TEXT_COLOR);
    }

    /** Explanatory text in the hint colour; wraps rather than truncates. */
    static WrappedText note(String text) {
        return new WrappedText(text, FontManager.getRunescapeSmallFont(), ColorScheme.MEDIUM_GRAY_COLOR);
    }

    static JLabel hint(String text) {
        JLabel label = new JLabel(text);
        label.setFont(FontManager.getRunescapeSmallFont());
        label.setForeground(ColorScheme.MEDIUM_GRAY_COLOR);
        return label;
    }
}
