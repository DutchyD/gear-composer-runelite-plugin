package dev.dutchy.runelite.gear.ui;

import dev.dutchy.runelite.gear.SectionId;
import dev.dutchy.runelite.gear.SetupOrder;

interface SectionActions {

    void addSetupTo(SectionId sectionId);

    /** Creates a setup already filled from what the player wears and carries. */
    void addSetupFromGame(SectionId sectionId);

    void renameSection(SectionId sectionId, String newName);

    void deleteSection(SectionId sectionId);

    void toggleSection(SectionId sectionId);

    void collapseOthers(SectionId sectionId);

    void moveSectionUp(SectionId sectionId);

    void moveSectionDown(SectionId sectionId);

    void insertSectionAbove(SectionId sectionId);

    void insertSectionBelow(SectionId sectionId);

    void exportSection(SectionId sectionId);

    void sortSection(SectionId sectionId, SetupOrder order);

    /** Creates an empty section one level under {@code parentId}. */
    void addSubSection(SectionId parentId);

    void nestSection(SectionId sectionId, SectionId parentId);

    void unnestSection(SectionId sectionId);
}
