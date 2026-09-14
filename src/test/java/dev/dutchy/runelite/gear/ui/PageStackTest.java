package dev.dutchy.runelite.gear.ui;

import dev.dutchy.runelite.gear.guide.GuideId;
import org.junit.jupiter.api.Test;

import javax.swing.JLabel;
import javax.swing.JPanel;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PageStackTest {

    private static final class HomePage extends JPanel {
    }

    private static final class DetailPage extends JPanel {
    }

    private final List<String> shown = new ArrayList<>();
    private final List<GuideId> guidesAsked = new ArrayList<>();
    private final PageStack pages = new PageStack("home", new HomePage(), guide -> {
        guidesAsked.add(guide);
        return new JLabel("?");
    }, shown::add);

    @Test
    void startsOnTheHomePageAndFindsPagesByType() {
        assertTrue(pages.isShowing("home"));
        assertTrue(pages.visible(HomePage.class).isPresent());
        assertTrue(pages.visible(DetailPage.class).isEmpty());

        DetailPage detail = new DetailPage();
        pages.replace("detail", detail);
        assertTrue(pages.visible(DetailPage.class).isEmpty(), "replacing does not show");
        assertSame(detail, pages.any(DetailPage.class).orElseThrow(), "but the page is there to be found");

        pages.show("detail");
        assertSame(detail, pages.visible(DetailPage.class).orElseThrow());
        assertTrue(pages.visible(HomePage.class).isEmpty());
        assertEquals(List.of("detail"), shown);
    }

    @Test
    void aReplacedPageTakesTheOldOnesPlace() {
        pages.replace("detail", new DetailPage());
        DetailPage newer = new DetailPage();
        pages.replace("detail", newer);
        pages.show("detail");
        assertSame(newer, pages.visible(DetailPage.class).orElseThrow());
        assertEquals(2, pages.component().getComponentCount(), "home and one detail page");
    }

    @Test
    void aPageShownOverAnotherRemembersWhatItCovers() {
        pages.replace("detail", new DetailPage());
        pages.show("detail");
        pages.replace("editor", new JPanel());
        pages.showOver("editor");
        assertEquals("detail", pages.beneath());

        pages.replace("editor", new JPanel());
        pages.showOver("editor");
        assertEquals("detail", pages.beneath(), "reopening the editor keeps the page under it");
        assertFalse(pages.isShowing("detail"));
    }

    @Test
    void aGuideButtonIsAskedForWhenAPageIsReplacedWithOne() {
        pages.replace("detail", new DetailPage(), GuideId.HISTORY);
        assertEquals(List.of(GuideId.HISTORY), guidesAsked);
    }
}
