package dev.dutchy.runelite.libs.ui.button;

import dev.dutchy.runelite.libs.ui.image.ImageTransforms;
import dev.dutchy.runelite.libs.ui.image.ItemImageOptions;
import dev.dutchy.runelite.libs.ui.item.ItemReference;
import dev.dutchy.runelite.libs.ui.item.ResolvedItem;
import dev.dutchy.runelite.libs.ui.style.ItemButtonStyle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.swing.SwingUtilities;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ItemButtonTest {

    private static final ResolvedItem WHIP = ResolvedItem.of(4151, "Abyssal whip");
    private static final ResolvedItem CAPE = ResolvedItem.of(6570, "Fire cape");

    private FakeItemLoader loader;
    private ItemButtonFactory factory;

    @BeforeEach
    void setUp() {
        loader = new FakeItemLoader();
        factory = new ItemButtonFactory(loader, ItemButtonStyle.runeLite());
    }

    @Test
    void buildingWithAReferenceStartsResolving() throws Exception {
        ItemButton button = onEdt(() -> factory.button(ItemReference.byName("whip")).build());
        assertEquals(ItemButtonState.RESOLVING, button.state());
        assertEquals("Loading \"whip\"", button.getToolTipText());
        assertEquals(1, loader.requests.size());
    }

    @Test
    void successfulLoadEndsReadyWithSpriteAndTooltip() throws Exception {
        ItemButton button = onEdt(() -> factory.button(4151).build());
        loader.last().completeWith(WHIP);
        flushEdt();
        assertEquals(ItemButtonState.READY, button.state());
        assertEquals(Optional.of(WHIP), button.item());
        assertEquals("Abyssal whip", button.getToolTipText());
    }

    @Test
    void unresolvedReferenceIsReportedInTooltip() throws Exception {
        ItemButton button = onEdt(() -> factory.button("Nonsense").build());
        loader.last().completeEmpty();
        flushEdt();
        assertEquals(ItemButtonState.UNRESOLVED, button.state());
        assertEquals("Unknown item: \"Nonsense\"", button.getToolTipText());
        assertTrue(button.item().isEmpty());
    }

    @Test
    void loaderFailureEndsInFailedState() throws Exception {
        ItemButton button = onEdt(() -> factory.button(1).build());
        loader.last().fail(new IllegalStateException("boom"));
        flushEdt();
        assertEquals(ItemButtonState.FAILED, button.state());
        assertEquals("Failed to load item #1", button.getToolTipText());
    }

    @Test
    void staleLoadsAreIgnoredAfterItemChanges() throws Exception {
        ItemButton button = onEdt(() -> factory.button(4151).build());
        FakeItemLoader.Request first = loader.last();
        onEdt(() -> {
            button.setItem(ItemReference.byId(6570));
            return null;
        });
        FakeItemLoader.Request second = loader.last();

        first.completeWith(WHIP);
        flushEdt();
        assertEquals(ItemButtonState.RESOLVING, button.state(), "stale result must not be applied");

        second.completeWith(CAPE);
        flushEdt();
        assertEquals(Optional.of(CAPE), button.item());
        assertEquals(ItemButtonState.READY, button.state());
    }

    @Test
    void clearItemDropsContentAndTooltip() throws Exception {
        ItemButton button = onEdt(() -> factory.button(4151).build());
        loader.last().completeWith(WHIP);
        flushEdt();
        onEdt(() -> {
            button.clearItem();
            return null;
        });
        assertEquals(ItemButtonState.EMPTY, button.state());
        assertTrue(button.reference().isEmpty());
        assertNull(button.getToolTipText());
    }

    @Test
    void customTooltipOverridesAutomaticOne() throws Exception {
        ItemButton button = onEdt(() -> factory.button(4151).tooltip("Equip").build());
        loader.last().completeWith(WHIP);
        flushEdt();
        assertEquals("Equip", button.getToolTipText());
    }

    @Test
    void changingImageOptionsReloadsWithNewOptions() throws Exception {
        ItemButton button = onEdt(() -> factory.button(995).quantity(1).build());
        onEdt(() -> {
            button.setImageOptions(new ItemImageOptions(10_000, true));
            return null;
        });
        assertEquals(2, loader.requests.size());
        assertEquals(new ItemImageOptions(10_000, true), loader.last().options());
    }

    @Test
    void clickListenersReceiveResolvedItem() throws Exception {
        List<ItemClickEvent> events = new ArrayList<>();
        ItemButton button = onEdt(() -> factory.button(4151).onClick(events::add).build());
        loader.last().completeWith(WHIP);
        flushEdt();

        onEdt(() -> {
            click(button, MouseEvent.BUTTON3);
            return null;
        });

        assertEquals(1, events.size());
        assertEquals(WHIP, events.get(0).item());
        assertTrue(events.get(0).isRightClick());
        assertFalse(events.get(0).isDoubleClick());
    }

    @Test
    void clicksAreSuppressedWhileUnresolvedOrDisabled() throws Exception {
        List<ItemClickEvent> events = new ArrayList<>();
        ItemButton button = onEdt(() -> factory.button(4151).onClick(events::add).build());
        onEdt(() -> {
            click(button, MouseEvent.BUTTON1);
            return null;
        });
        assertTrue(events.isEmpty(), "no item yet");

        loader.last().completeWith(WHIP);
        flushEdt();
        onEdt(() -> {
            button.setEnabled(false);
            click(button, MouseEvent.BUTTON1);
            return null;
        });
        assertTrue(events.isEmpty(), "disabled");
    }

    @Test
    void toggleButtonsFlipSelectionOnLeftClick() throws Exception {
        ItemButton button = onEdt(() -> factory.button(4151).toggle(true).build());
        loader.last().completeWith(WHIP);
        flushEdt();
        onEdt(() -> {
            click(button, MouseEvent.BUTTON1);
            return null;
        });
        assertTrue(button.isSelected());
        onEdt(() -> {
            click(button, MouseEvent.BUTTON3);
            return null;
        });
        assertTrue(button.isSelected(), "right click must not toggle");
    }

    @Test
    void preferredSizeFollowsStyle() throws Exception {
        ItemButton button = onEdt(() -> factory.emptyButton().style(ItemButtonStyle.compact()).build());
        assertEquals(new Dimension(26, 26), button.getPreferredSize());
        onEdt(() -> {
            button.setStyle(ItemButtonStyle.runeLite());
            return null;
        });
        assertEquals(new Dimension(40, 36), button.getPreferredSize());
    }

    @Test
    void userTransformRunsBeforeFitting() throws Exception {
        AtomicReference<BufferedImage> seen = new AtomicReference<>();
        ItemButton button = onEdt(() -> factory.button(4151)
                .transform(source -> {
                    seen.set(source);
                    return ImageTransforms.scaleToFit(200, 200).apply(source);
                })
                .build());
        loader.last().completeWith(WHIP);
        flushEdt();
        assertEquals(36, seen.get().getWidth());
        assertEquals(ItemButtonState.READY, button.state());
    }

    @Test
    void mutatorsRejectCallsOffTheEdt() throws Exception {
        ItemButton button = onEdt(() -> factory.emptyButton().build());
        assertThrows(IllegalStateException.class, () -> button.setItem(ItemReference.byId(1)));
        assertThrows(IllegalStateException.class, () -> factory.emptyButton().build());
    }

    private static void click(ItemButton button, int mouseButton) {
        button.setSize(button.getPreferredSize());
        MouseEvent event = new MouseEvent(button, MouseEvent.MOUSE_CLICKED, System.currentTimeMillis(), 0,
                5, 5, 1, false, mouseButton);
        for (var listener : button.getMouseListeners()) {
            listener.mouseClicked(event);
        }
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
