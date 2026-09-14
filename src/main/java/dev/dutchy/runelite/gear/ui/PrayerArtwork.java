package dev.dutchy.runelite.gear.ui;

import dev.dutchy.runelite.gear.Prayer;

import java.awt.image.BufferedImage;
import java.util.function.Consumer;

/** Supplies the in-game icon for a prayer. The callback runs on the EDT. */
public interface PrayerArtwork {

    void load(Prayer prayer, Consumer<BufferedImage> onLoaded);

    /** Draws nothing, for tests and previews. */
    static PrayerArtwork none() {
        return (prayer, onLoaded) -> {
        };
    }
}
