package dev.dutchy.runelite.libs.ui.selector;

import dev.dutchy.runelite.libs.ui.button.DefaultItemLoader;
import dev.dutchy.runelite.libs.ui.button.ItemButtonFactory;
import dev.dutchy.runelite.libs.ui.image.LoadedItemImage;
import dev.dutchy.runelite.libs.ui.item.ItemResolver;
import dev.dutchy.runelite.libs.ui.item.ResolvedItem;
import dev.dutchy.runelite.libs.ui.item.StaticItemResolver;
import dev.dutchy.runelite.libs.ui.search.CachingItemIndex;
import dev.dutchy.runelite.libs.ui.search.FuzzyItemSearch;
import dev.dutchy.runelite.libs.ui.search.ItemCatalog;
import dev.dutchy.runelite.libs.ui.search.ItemRanker;
import dev.dutchy.runelite.libs.ui.search.ItemSearch;
import dev.dutchy.runelite.libs.ui.search.SubsequenceNameScorer;
import dev.dutchy.runelite.libs.ui.style.ItemButtonStyle;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ItemSelectorTest {

    private static final ResolvedItem WHIP = ResolvedItem.of(4151, "Abyssal whip");
    private static final ResolvedItem D_SCIM = ResolvedItem.of(4587, "Dragon scimitar");
    private static final ResolvedItem R_SCIM = ResolvedItem.of(1333, "Rune scimitar");

    private ItemSelectorFactory factory;
    private List<ResolvedItem> selections;

    @BeforeEach
    void setUp() {
        ItemResolver resolver = StaticItemResolver.of(WHIP, D_SCIM, R_SCIM);
        ItemSearch search = new FuzzyItemSearch(new CachingItemIndex(ItemCatalog.of(List.of(WHIP, D_SCIM, R_SCIM))),
                new ItemRanker(new SubsequenceNameScorer()), resolver, Runnable::run);
        ItemButtonFactory buttons = new ItemButtonFactory(new DefaultItemLoader(resolver,
                request -> new LoadedItemImage(new BufferedImage(36, 32, BufferedImage.TYPE_INT_ARGB))), ItemButtonStyle.compact());
        factory = new ItemSelectorFactory(search, buttons);
        selections = new ArrayList<>();
    }

    private ItemSelector selector() throws Exception {
        return onEdt(() -> factory.selector().limit(5).onSelect(selections::add).build());
    }

    @Test
    void queryShowsRankedResults() throws Exception {
        ItemSelector selector = selector();
        onEdt(() -> { selector.setQuery("scim"); return null; });
        flushEdt();
        assertEquals(2, selector.resultCount());
    }

    @Test
    void enterSelectsFirstResultByDefault() throws Exception {
        ItemSelector selector = selector();
        onEdt(() -> { selector.setQuery("scim"); return null; });
        flushEdt();
        onEdt(() -> { selector.selectHighlighted(); return null; });
        assertEquals(List.of(R_SCIM), selections);
        assertEquals(Optional.of(R_SCIM), selector.selectedItem());
        assertEquals("Rune scimitar", selector.query());
        assertEquals(0, selector.resultCount(), "results collapse after selection");
    }

    @Test
    void arrowKeysMoveHighlightAndWrap() throws Exception {
        ItemSelector selector = selector();
        onEdt(() -> { selector.setQuery("scim"); return null; });
        flushEdt();
        onEdt(() -> {
            selector.highlightNext();
            selector.selectHighlighted();
            return null;
        });
        assertEquals(List.of(D_SCIM), selections);

        onEdt(() -> { selector.setQuery("scim"); return null; });
        flushEdt();
        onEdt(() -> {
            selector.highlightPrevious();
            selector.selectHighlighted();
            return null;
        });
        assertEquals(D_SCIM, selections.get(1), "previous from first wraps to last");
    }

    @Test
    void idQueryFindsItem() throws Exception {
        ItemSelector selector = selector();
        onEdt(() -> { selector.setQuery("4151"); return null; });
        flushEdt();
        onEdt(() -> { selector.selectHighlighted(); return null; });
        assertEquals(List.of(WHIP), selections);
    }

    @Test
    void clearResetsEverything() throws Exception {
        ItemSelector selector = selector();
        onEdt(() -> { selector.setQuery("whip"); return null; });
        flushEdt();
        onEdt(() -> { selector.selectHighlighted(); selector.clear(); return null; });
        assertTrue(selector.selectedItem().isEmpty());
        assertEquals("", selector.query());
        assertEquals(0, selector.resultCount());
    }

    @Test
    void staleSearchResultsAreDiscarded() throws Exception {
        AtomicReference<CompletableFuture<List<ResolvedItem>>> pending = new AtomicReference<>();
        ItemSearch slow = (query, limit) -> {
            CompletableFuture<List<ResolvedItem>> future = new CompletableFuture<>();
            pending.set(future);
            return future;
        };
        ItemButtonFactory buttons = new ItemButtonFactory(new DefaultItemLoader(StaticItemResolver.of(WHIP),
                request -> new LoadedItemImage(new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB))));
        ItemSelector selector = onEdt(() -> new ItemSelectorFactory(slow, buttons).selector().build());

        onEdt(() -> { selector.setQuery("wh"); return null; });
        CompletableFuture<List<ResolvedItem>> first = pending.get();
        onEdt(() -> { selector.setQuery("whip"); return null; });
        CompletableFuture<List<ResolvedItem>> second = pending.get();

        first.complete(List.of(WHIP, D_SCIM, R_SCIM));
        flushEdt();
        assertEquals(0, selector.resultCount(), "stale results ignored");

        second.complete(List.of(WHIP));
        flushEdt();
        assertEquals(1, selector.resultCount());
    }

    @Test
    void mutatorsRejectCallsOffTheEdt() throws Exception {
        ItemSelector selector = selector();
        assertThrows(IllegalStateException.class, () -> selector.setQuery("x"));
        assertThrows(IllegalStateException.class, () -> factory.selector().build());
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
    @Test
    void aSelectorThatKeepsItsResultsLeavesThemDraggableAfterSelection() throws Exception {
        List<ResolvedItem> pressed = new ArrayList<>();
        ItemRowDragListener drag = new ItemRowDragListener() {
            @Override
            public void pressed(ResolvedItem item, MouseEvent event) {
                pressed.add(item);
            }

            @Override
            public void dragged(ResolvedItem item, MouseEvent event) {
            }

            @Override
            public void released(ResolvedItem item, MouseEvent event) {
            }
        };
        ItemSelector selector = onEdt(() -> factory.selector().limit(5).onRowDrag(drag).keepResultsOnSelect().onSelect(selections::add).build());
        onEdt(() -> { selector.setQuery("scim"); return null; });
        flushEdt();
        onEdt(() -> { selector.select(D_SCIM); return null; });
        assertEquals(List.of(D_SCIM), selections);
        assertEquals(2, selector.resultCount(), "results stay to be dragged");
        assertEquals("scim", selector.query(), "the typed query is left alone");

        ItemSearchResultRow row = onEdt(() -> selector.rows().get(1));
        assertEquals(D_SCIM, row.item());
        onEdt(() -> {
            for (MouseListener listener : row.sprite().getMouseListeners()) {
                listener.mousePressed(new MouseEvent(row.sprite(), MouseEvent.MOUSE_PRESSED, 0,
                        InputEvent.BUTTON1_DOWN_MASK, 2, 2, 1, false, MouseEvent.BUTTON1));
            }
            return null;
        });
        assertEquals(List.of(D_SCIM), pressed, "a press on the sprite starts the drag");
    }

}
