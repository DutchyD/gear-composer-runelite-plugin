package dev.dutchy.runelite.gear.content;

/** Where a divider's text sits within the columns it spans. */
public enum TextAlign {
    LEFT("Left"),
    CENTRE("Centre"),
    RIGHT("Right");

    public static final TextAlign DEFAULT = LEFT;

    private final String displayName;

    TextAlign(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }
}
