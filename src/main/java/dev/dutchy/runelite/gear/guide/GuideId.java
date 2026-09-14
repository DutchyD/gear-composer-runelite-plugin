package dev.dutchy.runelite.gear.guide;

/** Every guide the plugin offers, page guides and tutorials alike. */
public enum GuideId {
    HOME("The accounts screen"),
    LIST("The setup list"),
    EDITOR("The setup editor"),
    CONTENTS_GEAR("A gear layout's page"),
    CONTENTS_BANK("A bank layout's page"),
    CONTENTS_CUSTOM("A custom layout's page"),
    SLOT("The slot editor"),
    DIVIDER("The divider editor"),
    COMPARE("Comparing setups"),
    SHARE("Sharing a setup"),
    HISTORY("History"),
    HOTKEY("Hotkeys"),
    IMPORT("Importing"),
    BACKUPS("Backups"),
    SHARE_CODE("Share codes"),
    CELL_SOURCE("Filling a cell"),
    TUTORIAL_GEAR("Tutorial: Gear Layout"),
    TUTORIAL_BANK("Tutorial: Bank Layout"),
    TUTORIAL_CUSTOM("Tutorial: Custom Layout");

    private final String title;

    GuideId(String title) {
        this.title = title;
    }

    public String title() {
        return title;
    }

    public boolean isTutorial() {
        return name().startsWith("TUTORIAL_");
    }
}
