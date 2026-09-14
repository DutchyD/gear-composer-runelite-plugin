package dev.dutchy.runelite.gear.ui;

import dev.dutchy.runelite.gear.content.EquipmentSlot;
import dev.dutchy.runelite.libs.ui.swing.EdtDispatch;
import net.runelite.api.gameval.SpriteID;
import net.runelite.client.game.SpriteManager;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.awt.image.BufferedImage;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

/** The empty-slot glyphs of the game's worn equipment screen. */
@Singleton
public final class RuneLiteEquipmentSlotArtwork implements EquipmentSlotArtwork {

    private static final int FIRST_FRAME = 0;
    private static final Map<EquipmentSlot, Integer> SPRITES = new EnumMap<>(EquipmentSlot.class);

    static {
        SPRITES.put(EquipmentSlot.HEAD, SpriteID.Wornicons._0);
        SPRITES.put(EquipmentSlot.CAPE, SpriteID.Wornicons._1);
        SPRITES.put(EquipmentSlot.AMULET, SpriteID.Wornicons._2);
        SPRITES.put(EquipmentSlot.WEAPON, SpriteID.Wornicons._3);
        SPRITES.put(EquipmentSlot.RING, SpriteID.Wornicons._4);
        SPRITES.put(EquipmentSlot.BODY, SpriteID.Wornicons._5);
        SPRITES.put(EquipmentSlot.SHIELD, SpriteID.Wornicons._6);
        SPRITES.put(EquipmentSlot.LEGS, SpriteID.Wornicons._7);
        SPRITES.put(EquipmentSlot.HANDS, SpriteID.Wornicons._8);
        SPRITES.put(EquipmentSlot.FEET, SpriteID.Wornicons._9);
        SPRITES.put(EquipmentSlot.AMMUNITION, SpriteID.Wornicons._10);
    }

    private final SpriteManager sprites;

    @Inject
    public RuneLiteEquipmentSlotArtwork(SpriteManager sprites) {
        this.sprites = Objects.requireNonNull(sprites, "sprites");
    }

    @Override
    public void load(EquipmentSlot slot, Consumer<BufferedImage> onLoaded) {
        Objects.requireNonNull(slot, "slot");
        Objects.requireNonNull(onLoaded, "onLoaded");
        sprites.getSpriteAsync(SPRITES.get(slot), FIRST_FRAME, image ->
                EdtDispatch.onEdt(() -> {
                    if (image != null) {
                        onLoaded.accept(image);
                    }
                }));
    }
}
