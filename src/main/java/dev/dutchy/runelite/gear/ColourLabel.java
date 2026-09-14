package dev.dutchy.runelite.gear;

/** A colour a setup can be labelled with; orange is reserved for the active highlight. */
public enum ColourLabel {
    NONE(0x000000),
    RED(0xD9534F),
    GREEN(0x5CB85C),
    BLUE(0x4AA3FF),
    PURPLE(0xA46BE0),
    TEAL(0x3BBFB0),
    YELLOW(0xE6D14A),
    PINK(0xE07BB3),
    GREY(0x9A9A9A);

    private final int rgb;

    ColourLabel(int rgb) {
        this.rgb = rgb;
    }

    public int rgb() {
        return rgb;
    }

    public boolean isNone() {
        return this == NONE;
    }
}
