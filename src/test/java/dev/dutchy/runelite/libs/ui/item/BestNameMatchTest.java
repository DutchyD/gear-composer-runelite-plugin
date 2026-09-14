package dev.dutchy.runelite.libs.ui.item;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BestNameMatchTest {

    private final BestNameMatch matcher = new BestNameMatch();

    private static final ResolvedItem RUNE_SCIMITAR = ResolvedItem.of(1333, "Rune scimitar");
    private static final ResolvedItem RUNE_SCIMITAR_ORNAMENT = ResolvedItem.of(23330, "Rune scimitar (guthix)");
    private static final ResolvedItem ADAMANT_SCIMITAR = ResolvedItem.of(1331, "Adamant scimitar");
    private static final ResolvedItem RUNE = ResolvedItem.of(9999, "Rune");

    @Test
    void prefersExactMatchIgnoringCase() {
        Optional<ResolvedItem> match = matcher.bestMatch("rune SCIMITAR",
                List.of(RUNE_SCIMITAR_ORNAMENT, ADAMANT_SCIMITAR, RUNE_SCIMITAR));
        assertEquals(Optional.of(RUNE_SCIMITAR), match);
    }

    @Test
    void prefersPrefixOverSubstring() {
        Optional<ResolvedItem> match = matcher.bestMatch("rune scim",
                List.of(ADAMANT_SCIMITAR, RUNE_SCIMITAR_ORNAMENT));
        assertEquals(Optional.of(RUNE_SCIMITAR_ORNAMENT), match);
    }

    @Test
    void breaksTiesByShorterNameThenLowerId() {
        Optional<ResolvedItem> match = matcher.bestMatch("scimitar",
                List.of(RUNE_SCIMITAR_ORNAMENT, ADAMANT_SCIMITAR, RUNE_SCIMITAR));
        assertEquals(Optional.of(RUNE_SCIMITAR), match);
    }

    @Test
    void ignoresCandidatesThatDoNotContainQuery() {
        assertTrue(matcher.bestMatch("whip", List.of(RUNE, ADAMANT_SCIMITAR)).isEmpty());
    }

    @Test
    void emptyCandidatesYieldEmpty() {
        assertTrue(matcher.bestMatch("anything", List.of()).isEmpty());
    }
}
