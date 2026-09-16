package dev.dutchy.runelite.gear.content;

public interface SetupContent {

    SetupType type();

    boolean isEmpty();

    static SetupContent empty(SetupType type) {
        switch (type) {
            case GEAR:
                return GearContent.empty();
            case BANK:
                return BankContent.empty();
            default:
                return CustomContent.empty();
        }
    }
}
