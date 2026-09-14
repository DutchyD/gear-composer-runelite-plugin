package dev.dutchy.runelite.gear;

import java.util.stream.Collectors;
import dev.dutchy.runelite.libs.ui.search.SubsequenceNameScorer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SetupFilterTest {

    private final SetupFilter filter = new SetupFilter(new SubsequenceNameScorer());
    private GearSetupBook book;

    @BeforeEach
    void setUp() {
        book = new GearSetupBook("Bosses");
        SectionId bosses = book.sections().get(0).id();
        SectionId skilling = book.addSection("Skilling").id();
        book.addSetup(bosses, "Vorkath");
        book.addSetup(bosses, "Zulrah");
        book.addSetup(skilling, "Wintertodt");
    }

    private List<String> names(List<GearSection> sections) {
        return sections.stream().flatMap(section -> section.setups().stream()).map(GearSetup::name).collect(Collectors.toList());
    }

    @Test
    void blankQueryReturnsEverything() {
        assertEquals(2, filter.apply(book.sections(), "   ").size());
        assertEquals(List.of("Vorkath", "Zulrah", "Wintertodt"), names(filter.apply(book.sections(), "")));
    }

    @Test
    void matchesBySubstringIgnoringCase() {
        assertEquals(List.of("Vorkath"), names(filter.apply(book.sections(), "VORK")));
    }

    @Test
    void matchesByAbbreviation() {
        assertEquals(List.of("Wintertodt"), names(filter.apply(book.sections(), "wtodt")));
    }

    @Test
    void dropsSectionsWithNoMatches() {
        List<GearSection> result = filter.apply(book.sections(), "wintertodt");
        assertEquals(1, result.size());
        assertEquals("Skilling", result.get(0).name());
    }

    @Test
    void keepsTheUserArrangedOrder() {
        assertEquals(List.of("Vorkath", "Zulrah"), names(filter.apply(book.sections(), "h")));
    }

    @Test
    void noMatchesYieldsNoSections() {
        assertTrue(filter.apply(book.sections(), "nothing here").isEmpty());
    }
}
