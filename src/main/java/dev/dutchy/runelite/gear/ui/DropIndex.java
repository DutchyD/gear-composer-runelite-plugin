package dev.dutchy.runelite.gear.ui;

import java.awt.*;
import java.util.List;

final class DropIndex {

    private DropIndex() {
    }

    /** Returns a value from 0 to sections.size(). */
    static int sectionInsertionIndex(List<Rectangle> sections, int y) {
        int index = 0;
        for (Rectangle bounds : sections) {
            if (y > bounds.y + bounds.height / 2) {
                index++;
            }
        }
        return index;
    }

    /** Returns a value from 0 to tiles.size(). */
    static int insertionIndex(List<Rectangle> tiles, Point drop) {
        for (int i = 0; i < tiles.size(); i++) {
            Rectangle bounds = tiles.get(i);
            boolean aboveThisRow = drop.y < bounds.y;
            boolean inThisRow = drop.y >= bounds.y && drop.y < bounds.y + bounds.height;
            boolean leftHalf = drop.x < bounds.x + bounds.width / 2;
            if (aboveThisRow || (inThisRow && leftHalf)) {
                return i;
            }
        }
        return tiles.size();
    }
}
