package dev.dutchy.runelite.libs.ui.search;

import org.junit.jupiter.api.Test;

import java.util.OptionalInt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SubsequenceNameScorerTest {

    private final SubsequenceNameScorer scorer = new SubsequenceNameScorer();

    private int score(String query, String name) {
        OptionalInt score = scorer.score(query, name);
        assertTrue(score.isPresent(), query + " should match " + name);
        return score.getAsInt();
    }

    @Test
    void tiersAreStrictlyOrdered() {
        int exact = score("Rune scimitar", "Rune scimitar");
        int prefix = score("Rune scim", "Rune scimitar");
        int wordStart = score("scim", "Rune scimitar");
        int substring = score("cimi", "Rune scimitar");
        int subsequence = score("rscim", "Rune scimitar");
        assertTrue(exact > prefix);
        assertTrue(prefix > wordStart);
        assertTrue(wordStart > substring);
        assertTrue(substring > subsequence);
    }

    @Test
    void ignoresCaseAndSurroundingWhitespace() {
        assertEquals(score("abyssal whip", "Abyssal whip"), score("  ABYSSAL WHIP ", "Abyssal whip"));
    }

    @Test
    void subsequenceMatchesAbbreviations() {
        assertTrue(scorer.score("dscim", "Dragon scimitar").isPresent());
        assertTrue(scorer.score("ppot", "Prayer potion(4)").isPresent());
    }

    @Test
    void tighterSubsequenceScoresHigher() {
        assertTrue(score("dsc", "Dragon scimitar") > score("dsc", "Dragon spear (kp) claw"));
    }

    @Test
    void shorterNamesWinWithinATier() {
        assertTrue(score("rune", "Rune bar") > score("rune", "Rune platebody (g)"));
    }

    @Test
    void outOfOrderCharactersDoNotMatch() {
        assertTrue(scorer.score("whip abyssal", "Abyssal whip").isEmpty());
        assertTrue(scorer.score("xyz", "Abyssal whip").isEmpty());
    }

    @Test
    void blankQueryMatchesEverythingWithZeroScore() {
        assertEquals(OptionalInt.of(0), scorer.score("  ", "Anything"));
    }
}
