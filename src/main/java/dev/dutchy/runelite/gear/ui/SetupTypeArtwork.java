package dev.dutchy.runelite.gear.ui;

import dev.dutchy.runelite.gear.content.SetupType;

import java.awt.image.BufferedImage;
import java.util.function.Consumer;

/** Supplies the picture shown on a setup type button. The callback runs on the EDT. */
public interface SetupTypeArtwork {

    void load(SetupType type, Consumer<BufferedImage> onLoaded);
}
