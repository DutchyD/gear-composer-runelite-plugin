package dev.dutchy.runelite.gear.content;

import java.util.Objects;

/** Overwrites part of a setup with what the player currently has. */
public final class LoadoutSync {

    private LoadoutSync() {
    }

    public static SetupContent apply(SetupContent content, SyncScope scope, Loadout loadout) {
        Objects.requireNonNull(content, "content");
        Objects.requireNonNull(scope, "scope");
        Objects.requireNonNull(loadout, "loadout");
        if (!scope.appliesTo(content)) {
            throw new IllegalArgumentException(scope + " does not apply to a " + content.type().displayName());
        }
        switch (scope) {
            case GEAR:
                return new GearContent(loadout.equipment(), loadout.inventory());
            case LEFT_SIDE:
                return ((BankContent) content).withLeft(loadout.inventory());
            default:
                return ((BankContent) content).withRight(loadout.inventory());
        }
    }
}
