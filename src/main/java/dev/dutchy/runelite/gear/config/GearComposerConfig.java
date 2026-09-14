package dev.dutchy.runelite.gear.config;

import dev.dutchy.runelite.gear.ui.TileStyle;
import net.runelite.client.config.*;

@SuppressWarnings("SameReturnValue")
@ConfigGroup(GearComposerConfig.GROUP)
public interface GearComposerConfig extends Config {

    String GROUP = "gearcomposer";
    String ONBOARDING_DISMISSED = "onboardingDismissed";
    String TILE_STYLE = "tileStyle";
    String COMPLETED_GUIDES = "completedGuides";
    String KNOWN_ACCOUNTS = "knownAccounts";

    @ConfigItem(
            keyName = "confirmBeforeSync",
            name = "Confirm before sync",
            description = "Ask before Sync from game overwrites a setup's slots",
            position = 1)
    default boolean confirmBeforeSync() {
        return true;
    }

    @Range(min = 2, max = 60)
    @ConfigItem(
            keyName = "undoToastSeconds",
            name = "Undo offer (seconds)",
            description = "How long the Undo link stays in the status line after a change",
            position = 2)
    default int undoToastSeconds() {
        return 8;
    }

    @ConfigItem(
            keyName = "clearLayoutOnBankClose",
            name = "Clear layout when the bank closes",
            description = "Turn the active setup off again as soon as the bank is closed",
            position = 3)
    default boolean clearLayoutOnBankClose() {
        return false;
    }

    @ConfigItem(
            keyName = "nextSetupHotkey",
            name = "Next setup",
            description = "Show the next setup in the bank",
            position = 10)
    default Keybind nextSetupHotkey() {
        return Keybind.NOT_SET;
    }

    @ConfigItem(
            keyName = "previousSetupHotkey",
            name = "Previous setup",
            description = "Show the previous setup in the bank",
            position = 11)
    default Keybind previousSetupHotkey() {
        return Keybind.NOT_SET;
    }

    @ConfigItem(
            keyName = "clearLayoutHotkey",
            name = "Clear layout",
            description = "Turn the active setup off",
            position = 12)
    default Keybind clearLayoutHotkey() {
        return Keybind.NOT_SET;
    }

    @ConfigItem(
            keyName = "nextVariantHotkey",
            name = "Next variant",
            description = "Show the active setup's next variant in the bank",
            position = 13)
    default Keybind nextVariantHotkey() {
        return Keybind.NOT_SET;
    }

    @ConfigItem(
            keyName = "previousVariantHotkey",
            name = "Previous variant",
            description = "Show the active setup's previous variant in the bank",
            position = 14)
    default Keybind previousVariantHotkey() {
        return Keybind.NOT_SET;
    }

    @ConfigItem(
            keyName = "tileStyle",
            name = "Setup list style",
            description = "Grid tiles, a denser grid, or a list with full names",
            position = 4)
    default TileStyle tileStyle() {
        return TileStyle.GRID;
    }

    @ConfigItem(
            keyName = "withdrawSetupAmount",
            name = "Left-click withdraws the setup amount",
            description = "While a setup is shown, the withdraw option matching its amount becomes the left-click",
            position = 5)
    default boolean withdrawSetupAmount() {
        return true;
    }

    @ConfigItem(
            keyName = "dimUnmatchedOnSearch",
            name = "Bank search dims the layout",
            description = "While a setup is shown, searching the bank fades items that do not match instead of hiding them",
            position = 6)
    default boolean dimUnmatchedOnSearch() {
        return true;
    }

    @ConfigItem(
            keyName = "tabBadges",
            name = "Show tab badges",
            description = "Mark each laid-out item with the bank tab it really lives in",
            position = 7)
    default boolean tabBadges() {
        return true;
    }

    @ConfigItem(
            keyName = "activationToast",
            name = "In-game notice on switch",
            description = "Briefly show which setup became active in the game window",
            position = 13)
    default boolean activationToast() {
        return true;
    }

    @ConfigItem(
            keyName = "shareLedger",
            name = "Ledger on shared setups",
            description = "Include value, weight and stats when sharing as an image or text",
            position = 15)
    default boolean shareLedger() {
        return true;
    }

    @ConfigItem(
            keyName = COMPLETED_GUIDES,
            name = "Completed guides",
            description = "Which guides have been run to the end",
            hidden = true)
    default String completedGuides() {
        return "";
    }

    @ConfigItem(
            keyName = KNOWN_ACCOUNTS,
            name = "Known accounts",
            description = "Character names seen so far, by profile key",
            hidden = true)
    default String knownAccounts() {
        return "";
    }

    @ConfigItem(
            keyName = ONBOARDING_DISMISSED,
            name = "Onboarding dismissed",
            description = "Whether the getting-started card has been closed",
            hidden = true)
    default boolean onboardingDismissed() {
        return false;
    }
}
