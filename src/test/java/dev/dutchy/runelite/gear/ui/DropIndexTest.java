package dev.dutchy.runelite.gear.ui;

import org.junit.jupiter.api.Test;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DropIndexTest {

    private static final List<Rectangle> GRID = List.of(
            new Rectangle(0, 0, 46, 52), new Rectangle(49, 0, 46, 52),
            new Rectangle(98, 0, 46, 52), new Rectangle(147, 0, 46, 52),
            new Rectangle(0, 55, 46, 52), new Rectangle(49, 55, 46, 52));

    @Test
    void dropOnLeftHalfInsertsBeforeThatTile() {
        assertEquals(1, DropIndex.insertionIndex(GRID, new Point(55, 20)));
    }

    @Test
    void dropOnRightHalfInsertsAfterThatTile() {
        assertEquals(2, DropIndex.insertionIndex(GRID, new Point(90, 20)));
    }

    @Test
    void dropAboveTheFirstRowGoesToTheStart() {
        assertEquals(0, DropIndex.insertionIndex(GRID, new Point(120, -5)));
    }

    @Test
    void dropOnTheSecondRowSkipsTheFirst() {
        assertEquals(4, DropIndex.insertionIndex(GRID, new Point(5, 70)));
        assertEquals(5, DropIndex.insertionIndex(GRID, new Point(60, 70)));
    }

    @Test
    void dropPastTheLastTileGoesToTheEnd() {
        assertEquals(6, DropIndex.insertionIndex(GRID, new Point(180, 70)));
        assertEquals(6, DropIndex.insertionIndex(GRID, new Point(20, 400)));
    }

    private static final List<Rectangle> SECTIONS = List.of(
            new Rectangle(0, 0, 200, 60),
            new Rectangle(0, 60, 200, 90),
            new Rectangle(0, 150, 200, 40));

    @Test
    void sectionDropAboveAMidpointInsertsBeforeIt() {
        assertEquals(0, DropIndex.sectionInsertionIndex(SECTIONS, 10));
        assertEquals(1, DropIndex.sectionInsertionIndex(SECTIONS, 40));
    }

    @Test
    void sectionDropBelowEverythingGoesLast() {
        assertEquals(3, DropIndex.sectionInsertionIndex(SECTIONS, 300));
    }

    @Test
    void sectionDropInTheMiddleLandsBetween() {
        assertEquals(2, DropIndex.sectionInsertionIndex(SECTIONS, 120));
    }

    @Test
    void noSectionsAlwaysTakesTheFirstPosition() {
        assertEquals(0, DropIndex.sectionInsertionIndex(List.of(), 50));
    }

    @Test
    void emptyGridAlwaysTakesTheFirstPosition() {
        assertEquals(0, DropIndex.insertionIndex(List.of(), new Point(30, 30)));
    }
}
