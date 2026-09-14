package dev.dutchy.runelite.gear.bank;

import lombok.Value;
import lombok.experimental.Accessors;

import java.util.Objects;

/** A scroll arrow at one end of the variant strip, drawn only when there is somewhere to scroll. */
@Value
@Accessors(fluent = true)
public class ArrowFace {
    boolean forward;
    String glyph;
    int x;
    int y;
    int width;
    int height;

    public ArrowFace(boolean forward, String glyph, int x, int y, int width, int height) {
        this.forward = forward;
        this.glyph = Objects.requireNonNull(glyph, "glyph");
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    /** How far the strip scrolls when this arrow is clicked. */
    public int step() {
        return forward ? 1 : -1;
    }
}
