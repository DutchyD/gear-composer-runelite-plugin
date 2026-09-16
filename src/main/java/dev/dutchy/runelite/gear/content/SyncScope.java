package dev.dutchy.runelite.gear.content;

/** Which part of a setup a sync from the game overwrites. */
public enum SyncScope {
    GEAR,
    LEFT_SIDE,
    RIGHT_SIDE;

    public boolean appliesTo(SetupContent content) {
        return this == GEAR ? content instanceof GearContent : content instanceof BankContent;
    }
}
