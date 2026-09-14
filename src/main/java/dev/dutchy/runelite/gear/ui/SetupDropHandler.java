package dev.dutchy.runelite.gear.ui;

import dev.dutchy.runelite.gear.SectionId;
import dev.dutchy.runelite.gear.SetupId;

@FunctionalInterface
interface SetupDropHandler {

    void onSetupDropped(SetupId setupId, SectionId targetSection, int targetIndex);
}
