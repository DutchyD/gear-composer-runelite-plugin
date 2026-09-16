package dev.dutchy.runelite.gear.ui;

import net.runelite.client.ui.ColorScheme;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

final class SectionListPanel extends JPanel {

    private static final int CARET_THICKNESS = 2;

    private int caretIndex = -1;

    SectionListPanel() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(ColorScheme.DARK_GRAY_COLOR);
    }

    List<SectionView> sectionViews() {
        return Stream.of(getComponents())
                .filter(SectionView.class::isInstance)
                .map(SectionView.class::cast)
                .collect(Collectors.toList());
    }

    List<Rectangle> sectionBounds() {
        return sectionViews().stream().map(Component::getBounds).collect(Collectors.toList());
    }

    void showCaret(int index) {
        if (caretIndex != index) {
            caretIndex = index;
            repaint();
        }
    }

    void clearCaret() {
        showCaret(-1);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        List<Rectangle> bounds = sectionBounds();
        if (caretIndex < 0 || bounds.isEmpty()) {
            return;
        }
        int y = caretIndex < bounds.size()
                ? bounds.get(caretIndex).y
                : bounds.get(bounds.size() - 1).y + bounds.get(bounds.size() - 1).height;
        g.setColor(ColorScheme.BRAND_ORANGE);
        g.fillRect(0, Math.max(0, y - CARET_THICKNESS / 2), getWidth(), CARET_THICKNESS);
    }
}
