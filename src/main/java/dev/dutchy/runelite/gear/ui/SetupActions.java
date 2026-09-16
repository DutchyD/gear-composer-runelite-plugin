package dev.dutchy.runelite.gear.ui;

import dev.dutchy.runelite.gear.ColourLabel;
import dev.dutchy.runelite.gear.GearSetup;
import dev.dutchy.runelite.gear.Owner;

import java.util.List;

interface SetupActions {

    void activate(GearSetup setup);

    void edit(GearSetup setup);

    void editContents(GearSetup setup);

    /** Makes the variant the setup's chosen one, so the bank shows it. */
    void selectVariant(GearSetup setup, int index);

    /** Opens the setup's contents and adds a variant that starts as a copy of the chosen one. */
    void addVariant(GearSetup setup);

    void delete(GearSetup setup);

    /** Every owner the setup could move to, the shared owner included, without its current one. */
    List<Owner> otherOwners(GearSetup setup);

    String describeOwner(Owner owner);

    void moveTo(GearSetup setup, Owner owner);

    void duplicate(GearSetup setup);

    void togglePin(GearSetup setup);

    void setLabel(GearSetup setup, ColourLabel label);

    /** Opens the key capture for the setup's hotkey. */
    void chooseHotkey(GearSetup setup);
}
