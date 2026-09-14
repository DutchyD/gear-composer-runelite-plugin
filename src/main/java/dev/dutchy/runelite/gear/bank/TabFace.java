package dev.dutchy.runelite.gear.bank;

import lombok.Value;
import lombok.experimental.Accessors;

import java.util.Objects;

/** One variant tab in the strip, with its name already escaped for the game's renderer. */
@Value
@Accessors(fluent = true)
public class TabFace {
    String name;
    int variant;
    boolean chosen;
    int x;
    int y;
    int width;
    int height;

    public TabFace(String name, int variant, boolean chosen, int x, int y, int width, int height) {
        this.name = Objects.requireNonNull(name, "name");
        this.variant = variant;
        this.chosen = chosen;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }
}
