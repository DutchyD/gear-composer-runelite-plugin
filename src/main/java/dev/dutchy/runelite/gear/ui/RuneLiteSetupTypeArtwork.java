package dev.dutchy.runelite.gear.ui;

import dev.dutchy.runelite.gear.content.SetupType;
import dev.dutchy.runelite.libs.ui.image.ImageTransforms;
import dev.dutchy.runelite.libs.ui.swing.EdtDispatch;
import net.runelite.api.gameval.SpriteID;
import net.runelite.client.game.SpriteManager;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Objects;
import java.util.function.Consumer;

/** Game sprites for the type buttons; the custom layout stacks the equipment and inventory icons, since it is several of each. */
@Singleton
public final class RuneLiteSetupTypeArtwork implements SetupTypeArtwork {

    private static final int FIRST_FRAME = 0;
    static final int STACK_SIZE = 40;
    static final int STACK_ICON = 28;

    private final SpriteManager sprites;

    @Inject
    public RuneLiteSetupTypeArtwork(SpriteManager sprites) {
        this.sprites = Objects.requireNonNull(sprites, "sprites");
    }

    @Override
    public void load(SetupType type, Consumer<BufferedImage> onLoaded) {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(onLoaded, "onLoaded");
        if (type == SetupType.CUSTOM) {
            sprite(SpriteID.SideiconsInterface.EQUIPMENT, equipment ->
                    sprite(SpriteID.SideiconsInterface.INVENTORY, inventory ->
                            onLoaded.accept(stacked(equipment, inventory))));
            return;
        }
        sprite(type == SetupType.GEAR ? SpriteID.SideiconsInterface.EQUIPMENT : SpriteID.CHEST, onLoaded);
    }

    private void sprite(int spriteId, Consumer<BufferedImage> onLoaded) {
        sprites.getSpriteAsync(spriteId, FIRST_FRAME, image ->
                EdtDispatch.onEdt(() -> {
                    if (image != null) {
                        onLoaded.accept(image);
                    }
                }));
    }

    /** The first icon top left, the second overlapping it bottom right, the way a pile of things is drawn. */
    static BufferedImage stacked(BufferedImage back, BufferedImage front) {
        BufferedImage image = new BufferedImage(STACK_SIZE, STACK_SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        try {
            BufferedImage backIcon = ImageTransforms.scaleToFit(STACK_ICON, STACK_ICON).apply(back);
            BufferedImage frontIcon = ImageTransforms.scaleToFit(STACK_ICON, STACK_ICON).apply(front);
            g.drawImage(backIcon, (STACK_ICON - backIcon.getWidth()) / 2, (STACK_ICON - backIcon.getHeight()) / 2, null);
            int offset = STACK_SIZE - STACK_ICON;
            g.drawImage(frontIcon, offset + (STACK_ICON - frontIcon.getWidth()) / 2, offset + (STACK_ICON - frontIcon.getHeight()) / 2, null);
        } finally {
            g.dispose();
        }
        return image;
    }
}
