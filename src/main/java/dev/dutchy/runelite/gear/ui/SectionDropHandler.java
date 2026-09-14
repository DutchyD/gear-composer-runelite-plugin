package dev.dutchy.runelite.gear.ui;

import dev.dutchy.runelite.gear.SectionId;

@FunctionalInterface
interface SectionDropHandler {

    void onSectionDropped(SectionId sectionId, int targetIndex);
}
