package dev.dutchy.runelite.gear.ui;

import dev.dutchy.runelite.gear.content.EquipmentSlot;

import java.awt.image.BufferedImage;
import java.util.function.Consumer;

/** Supplies the in-game glyph drawn in an empty worn slot. The callback runs on the EDT. */
public interface EquipmentSlotArtwork {

    void load(EquipmentSlot slot, Consumer<BufferedImage> onLoaded);

    /** Draws nothing, for tests and previews. */
    static EquipmentSlotArtwork none() {
        return (slot, onLoaded) -> {
        };
    }
}
