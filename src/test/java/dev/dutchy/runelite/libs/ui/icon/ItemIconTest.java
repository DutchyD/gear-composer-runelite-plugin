package dev.dutchy.runelite.libs.ui.icon;

import dev.dutchy.runelite.libs.ui.button.DefaultItemLoader;
import dev.dutchy.runelite.libs.ui.image.LoadedItemImage;
import dev.dutchy.runelite.libs.ui.item.ItemReference;
import dev.dutchy.runelite.libs.ui.item.ResolvedItem;
import dev.dutchy.runelite.libs.ui.item.StaticItemResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.swing.SwingUtilities;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ItemIconTest {

    private static final ResolvedItem WHIP = ResolvedItem.of(4151, "Abyssal whip");

    private ItemIconFactory factory;

    @BeforeEach
    void setUp() {
        factory = new ItemIconFactory(new DefaultItemLoader(StaticItemResolver.of(WHIP),
                request -> new LoadedItemImage(new BufferedImage(36, 32, BufferedImage.TYPE_INT_ARGB))));
    }

    @Test
    void showsTheSpriteOnceLoaded() throws Exception {
        ItemIcon icon = onEdt(() -> factory.icon(32, 32));
        onEdt(() -> {
            icon.setItem(ItemReference.byId(4151));
            return null;
        });
        flushEdt();

        assertTrue(icon.hasSprite());
        assertEquals(Optional.of(WHIP), icon.item());
    }

    @Test
    void unknownItemsLeaveTheIconBlank() throws Exception {
        ItemIcon icon = onEdt(() -> factory.icon(32, 32));
        onEdt(() -> {
            icon.setItem(ItemReference.byId(1));
            return null;
        });
        flushEdt();

        assertFalse(icon.hasSprite());
        assertTrue(icon.item().isEmpty());
    }

    @Test
    void clearingRemovesTheSprite() throws Exception {
        ItemIcon icon = onEdt(() -> factory.icon(32, 32));
        onEdt(() -> {
            icon.setItem(ItemReference.byId(4151));
            return null;
        });
        flushEdt();
        onEdt(() -> {
            icon.clearItem();
            return null;
        });

        assertFalse(icon.hasSprite());
        assertTrue(icon.item().isEmpty());
    }

    @Test
    void sizeIsFixedSoGridsStayAligned() throws Exception {
        ItemIcon icon = onEdt(() -> factory.icon(32, 24));
        assertEquals(new Dimension(32, 24), icon.getPreferredSize());
        assertEquals(new Dimension(32, 24), icon.getMaximumSize());
    }

    @Test
    void validatesArgumentsAndThreading() throws Exception {
        ItemIcon icon = onEdt(() -> factory.icon(32, 32));
        assertThrows(IllegalStateException.class, () -> icon.setItem(ItemReference.byId(4151)));
        assertThrows(IllegalStateException.class, () -> factory.icon(32, 32));
        assertThrows(IllegalArgumentException.class, () -> {
            try {
                onEdt(() -> factory.icon(0, 32));
            } catch (Exception e) {
                throw e instanceof RuntimeException ? (RuntimeException) e : new RuntimeException(e);
            }
        });
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

    private static void flushEdt() throws Exception {
        for (int i = 0; i < 3; i++) {
            SwingUtilities.invokeAndWait(() -> { });
        }
    }
}
