package dev.dutchy.runelite.gear.ui;

import dev.dutchy.runelite.gear.ColourLabel;
import net.runelite.client.ui.ColorScheme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Objects;

/** One clickable colour dot; the "none" dot is a struck-through ring. */
final class ColourSwatch extends JComponent {

    static final int SIZE = 14;

    private final ColourLabel label;
    private final Runnable onPick;
    private boolean chosen;
    private boolean hovered;

    ColourSwatch(ColourLabel label, boolean chosen, Runnable onPick) {
        this.label = Objects.requireNonNull(label, "label");
        this.chosen = chosen;
        this.onPick = Objects.requireNonNull(onPick, "onPick");
        setPreferredSize(new Dimension(SIZE, SIZE));
        setToolTipText(label.isNone() ? "No colour" : label.name().charAt(0) + label.name().substring(1).toLowerCase());
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                pick();
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                hovered = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hovered = false;
                repaint();
            }
        });
    }

    ColourLabel label() {
        return label;
    }

    void setChosen(boolean isChosen) {
        chosen = isChosen;
        repaint();
    }

    void pick() {
        onPick.run();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (label.isNone()) {
                g2.setColor(Ui.OUTLINE);
                g2.drawOval(2, 2, SIZE - 5, SIZE - 5);
                g2.drawLine(4, SIZE - 4, SIZE - 4, 4);
            } else {
                g2.setColor(new Color(label.rgb()));
                g2.fillOval(2, 2, SIZE - 4, SIZE - 4);
            }
            if (chosen || hovered) {
                g2.setColor(chosen ? ColorScheme.TEXT_COLOR : ColorScheme.LIGHT_GRAY_COLOR);
                g2.drawOval(0, 0, SIZE - 1, SIZE - 1);
            }
        } finally {
            g2.dispose();
        }
    }
}
