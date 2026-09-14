package dev.dutchy.runelite.gear.ui;

import dev.dutchy.runelite.gear.ColourLabel;
import dev.dutchy.runelite.gear.GearSetup;
import dev.dutchy.runelite.gear.Owner;
import dev.dutchy.runelite.gear.content.GridKind;
import dev.dutchy.runelite.gear.content.SetupContentEditor;
import dev.dutchy.runelite.gear.content.SetupItem;
import dev.dutchy.runelite.gear.content.SlotRef;
import dev.dutchy.runelite.libs.ui.button.DefaultItemLoader;
import dev.dutchy.runelite.libs.ui.icon.ItemIconFactory;
import dev.dutchy.runelite.libs.ui.image.LoadedItemImage;
import dev.dutchy.runelite.libs.ui.item.ResolvedItem;
import dev.dutchy.runelite.libs.ui.item.StaticItemResolver;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SetupTileTest {

    private ItemIconFactory icons;

    private static final SetupActions NO_ACTIONS = new SetupActions() {
        @Override
        public void activate(GearSetup setup) {
        }

        @Override
        public void edit(GearSetup setup) {
        }

        @Override
        public void editContents(GearSetup setup) {
        }

        @Override
        public void selectVariant(GearSetup setup, int index) {
        }

        @Override
        public void addVariant(GearSetup setup) {
        }

        @Override
        public void delete(GearSetup setup) {
        }

        @Override
        public List<Owner> otherOwners(GearSetup setup) {
            return List.of();
        }

        @Override
        public String describeOwner(Owner owner) {
            return "";
        }

        @Override
        public void moveTo(GearSetup setup, Owner owner) {
        }

        @Override
        public void duplicate(GearSetup setup) {
        }

        @Override
        public void togglePin(GearSetup setup) {
        }

        @Override
        public void setLabel(GearSetup setup, ColourLabel label) {
        }

        @Override
        public void chooseHotkey(GearSetup setup) {
        }

        @Override
        public void copyShareCode(GearSetup setup) {
        }

        @Override
        public void compare(GearSetup setup) {
        }

        @Override
        public void share(GearSetup setup) {
        }
    };

    private static final BulkSelector ALWAYS_SELECTED = new BulkSelector() {
        @Override
        public boolean isSelected(BulkTarget target) {
            return true;
        }

        @Override
        public void select(BulkTarget target, boolean shiftDown) {
        }
    };

    @BeforeEach
    void setUp() {
        icons = new ItemIconFactory(new DefaultItemLoader(
                StaticItemResolver.of(ResolvedItem.of(4151, "Abyssal whip")),
                request -> new LoadedItemImage(new BufferedImage(36, 32, BufferedImage.TYPE_INT_ARGB))));
    }

    @Test
    void repaintingASelectedTileDoesNotDeepenItsHighlight() throws Exception {
        SetupTile tile = onEdt(() -> {
            SetupTile created = new SetupTile(GearSetup.named("Vorkath"), icons, NO_ACTIONS, null, ALWAYS_SELECTED);
            created.setSize(created.getPreferredSize());
            return created;
        });

        BufferedImage canvas = new BufferedImage(tile.getWidth(), tile.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = canvas.createGraphics();
        try {
            tile.paint(g);
            int[] afterFirstPaint = pixels(canvas);
            tile.paint(g);
            int[] afterSecondPaint = pixels(canvas);
            tile.paint(g);
            int[] afterThirdPaint = pixels(canvas);

            assertArrayEquals(afterFirstPaint, afterSecondPaint, "the highlight stacked on repaint");
            assertArrayEquals(afterFirstPaint, afterThirdPaint, "the highlight stacked on repaint");
        } finally {
            g.dispose();
        }
    }

    @Test
    void aSelectedTilePaintsAnOpaqueBackground() throws Exception {
        SetupTile tile = onEdt(() ->
                new SetupTile(GearSetup.named("Vorkath"), icons, NO_ACTIONS, null, ALWAYS_SELECTED));
        assertFalse(tile.getBackground().getAlpha() < 255, "an opaque component must not use a translucent background");
    }

    private static int[] pixels(BufferedImage image) {
        return image.getRGB(0, 0, image.getWidth(), image.getHeight(), null, 0, image.getWidth());
    }

    private static <T> T onEdt(Callable<T> action) throws Exception {
        AtomicReference<T> result = new AtomicReference<>();
        AtomicReference<Exception> failure = new AtomicReference<>();
        SwingUtilities.invokeAndWait(() -> {
            try {
                result.set(action.call());
            } catch (Exception e) {
                failure.set(e);
            }
        });
        if (failure.get() != null) {
            throw failure.get();
        }
        return result.get();
    }
    @Test
    void everyStyleLaysOutAndPaints() throws Exception {
        for (TileStyle style : TileStyle.values()) {
            SetupTile tile = onEdt(() -> {
                SetupTile created = new SetupTile(GearSetup.named("Vorkath"), icons, NO_ACTIONS, null, ALWAYS_SELECTED, style, HoverPreview.none());
                created.setSize(style == TileStyle.LIST ? new Dimension(200, SetupTile.LIST_HEIGHT) : created.getPreferredSize());
                return created;
            });
            BufferedImage canvas = new BufferedImage(tile.getWidth(), tile.getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics2D g = canvas.createGraphics();
            try {
                tile.paint(g);
            } finally {
                g.dispose();
            }
            assertEquals(style, tile.style());
        }
    }

    @Test
    void hoveringArmsThePreviewAndLeavingHidesIt() throws Exception {
        HoverPreview preview = new HoverPreview(icons);
        SetupTile tile = onEdt(() -> new SetupTile(GearSetup.named("Vorkath"), icons, NO_ACTIONS, null, null, TileStyle.GRID, preview));
        onEdt(() -> {
            for (MouseListener listener : tile.getMouseListeners()) {
                listener.mouseEntered(new MouseEvent(tile, MouseEvent.MOUSE_ENTERED, 0, 0, 1, 1, 0, false));
            }
            return null;
        });
        assertTrue(preview.isArmed());
        onEdt(() -> {
            for (MouseListener listener : tile.getMouseListeners()) {
                listener.mouseExited(new MouseEvent(tile, MouseEvent.MOUSE_EXITED, 0, 0, 1, 1, 0, false));
            }
            return null;
        });
        assertFalse(preview.isArmed());
        assertFalse(preview.isShowing());
    }

    @Test
    void theMiniPreviewCountsTheItems() throws Exception {
        GearSetup setup = GearSetup.named("Vorkath").withContent(
                SetupContentEditor.withItem(GearSetup.named("Vorkath").content(),
                        SlotRef.of(GridKind.INVENTORY, 3),
                        SetupItem.of(4151)));
        MiniContentView view = onEdt(() -> new MiniContentView(setup, icons));
        assertTrue(labelsIn(view).contains("1 item"));
    }

    private static List<String> labelsIn(Container container) {
        List<String> found = new ArrayList<>();
        for (Component child : container.getComponents()) {
            if (child instanceof JLabel && ((JLabel) child).getText() != null) {
                found.add(((JLabel) child).getText());
            }
            if (child instanceof Container) {
                found.addAll(labelsIn((Container) child));
            }
        }
        return found;
    }

}
