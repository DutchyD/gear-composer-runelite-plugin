package dev.dutchy.runelite.gear.ui;

interface BulkSelector {

    boolean isSelected(BulkTarget target);

    void select(BulkTarget target, boolean shiftDown);
}
