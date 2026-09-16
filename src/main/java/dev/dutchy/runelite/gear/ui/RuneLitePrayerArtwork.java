package dev.dutchy.runelite.gear.ui;

import dev.dutchy.runelite.gear.Prayer;
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

/** The lit prayer-book icons, the same sprites the game's prayer tab draws. */
@Singleton
public final class RuneLitePrayerArtwork implements PrayerArtwork {

    private static final int FIRST_FRAME = 0;
    private static final Map<Prayer, Integer> SPRITES = new EnumMap<>(Prayer.class);

    static {
        SPRITES.put(Prayer.THICK_SKIN, SpriteID.Prayeron.THICK_SKIN);
        SPRITES.put(Prayer.BURST_OF_STRENGTH, SpriteID.Prayeron.BURST_OF_STRENGTH);
        SPRITES.put(Prayer.CLARITY_OF_THOUGHT, SpriteID.Prayeron.CLARITY_OF_THOUGHT);
        SPRITES.put(Prayer.ROCK_SKIN, SpriteID.Prayeron.ROCK_SKIN);
        SPRITES.put(Prayer.SUPERHUMAN_STRENGTH, SpriteID.Prayeron.SUPERHUMAN_STRENGTH);
        SPRITES.put(Prayer.IMPROVED_REFLEXES, SpriteID.Prayeron.IMPROVED_REFLEXES);
        SPRITES.put(Prayer.RAPID_RESTORE, SpriteID.Prayeron.RAPID_RESTORE);
        SPRITES.put(Prayer.RAPID_HEAL, SpriteID.Prayeron.RAPID_HEAL);
        SPRITES.put(Prayer.PROTECT_ITEM, SpriteID.Prayeron.PROTECT_ITEM);
        SPRITES.put(Prayer.STEEL_SKIN, SpriteID.Prayeron.STEEL_SKIN);
        SPRITES.put(Prayer.ULTIMATE_STRENGTH, SpriteID.Prayeron.ULTIMATE_STRENGTH);
        SPRITES.put(Prayer.INCREDIBLE_REFLEXES, SpriteID.Prayeron.INCREDIBLE_REFLEXES);
        SPRITES.put(Prayer.PROTECT_FROM_MAGIC, SpriteID.Prayeron.PROTECT_FROM_MAGIC);
        SPRITES.put(Prayer.PROTECT_FROM_MISSILES, SpriteID.Prayeron.PROTECT_FROM_MISSILES);
        SPRITES.put(Prayer.PROTECT_FROM_MELEE, SpriteID.Prayeron.PROTECT_FROM_MELEE);
        SPRITES.put(Prayer.RETRIBUTION, SpriteID.Prayeron.RETRIBUTION);
        SPRITES.put(Prayer.REDEMPTION, SpriteID.Prayeron.REDEMPTION);
        SPRITES.put(Prayer.SMITE, SpriteID.Prayeron.SMITE);
        SPRITES.put(Prayer.SHARP_EYE, SpriteID.Prayeron.SHARP_EYE);
        SPRITES.put(Prayer.MYSTIC_WILL, SpriteID.Prayeron.MYSTIC_WILL);
        SPRITES.put(Prayer.HAWK_EYE, SpriteID.Prayeron.HAWK_EYE);
        SPRITES.put(Prayer.MYSTIC_LORE, SpriteID.Prayeron.MYSTIC_LORE);
        SPRITES.put(Prayer.EAGLE_EYE, SpriteID.Prayeron.EAGLE_EYE);
        SPRITES.put(Prayer.MYSTIC_MIGHT, SpriteID.Prayeron.MYSTIC_MIGHT);
        SPRITES.put(Prayer.CHIVALRY, SpriteID.Prayeron.CHIVALRY);
        SPRITES.put(Prayer.PIETY, SpriteID.Prayeron.PIETY);
        SPRITES.put(Prayer.PRESERVE, SpriteID.Prayeron.PRESERVE);
        SPRITES.put(Prayer.RIGOUR, SpriteID.Prayeron.RIGOUR);
        SPRITES.put(Prayer.AUGURY, SpriteID.Prayeron.AUGURY);
        SPRITES.put(Prayer.DEADEYE, SpriteID.Prayeron.DEADEYE);
        SPRITES.put(Prayer.MYSTIC_VIGOUR, SpriteID.Prayeron.MYSTIC_VIGOUR);
    }

    private final SpriteManager sprites;

    @Inject
    public RuneLitePrayerArtwork(SpriteManager sprites) {
        this.sprites = Objects.requireNonNull(sprites, "sprites");
    }

    @Override
    public void load(Prayer prayer, Consumer<BufferedImage> onLoaded) {
        Objects.requireNonNull(prayer, "prayer");
        Objects.requireNonNull(onLoaded, "onLoaded");
        sprites.getSpriteAsync(SPRITES.get(prayer), FIRST_FRAME, image ->
                EdtDispatch.onEdt(() -> {
                    if (image != null) {
                        onLoaded.accept(image);
                    }
                }));
    }
}
