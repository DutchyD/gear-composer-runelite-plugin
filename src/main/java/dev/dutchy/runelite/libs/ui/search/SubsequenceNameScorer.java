package dev.dutchy.runelite.libs.ui.search;

import java.util.Locale;
import java.util.OptionalInt;

public final class SubsequenceNameScorer implements NameScorer {

    private static final int EXACT = 1000;
    private static final int PREFIX = 900;
    private static final int WORD_START = 800;
    private static final int SUBSTRING = 700;
    private static final int SUBSEQUENCE = 500;

    private static final int MAX_POSITION_PENALTY = 50;
    private static final int MAX_LENGTH_PENALTY = 60;
    private static final int GAP_PENALTY = 4;
    private static final int MAX_GAP_PENALTY = 200;

    @Override
    public OptionalInt score(String query, String candidateName) {
        String needle = normalise(query);
        String haystack = normalise(candidateName);
        if (needle.isEmpty()) {
            return OptionalInt.of(0);
        }
        if (haystack.equals(needle)) {
            return OptionalInt.of(EXACT);
        }
        int lengthPenalty = Math.min(haystack.length() - needle.length(), MAX_LENGTH_PENALTY);
        if (haystack.startsWith(needle)) {
            return OptionalInt.of(PREFIX - lengthPenalty);
        }
        int index = haystack.indexOf(needle);
        if (index >= 0) {
            int tier = isWordStart(haystack, index) ? WORD_START : SUBSTRING;
            return OptionalInt.of(tier - Math.min(index, MAX_POSITION_PENALTY) - lengthPenalty);
        }
        int gaps = subsequenceGaps(needle, haystack);
        if (gaps < 0) {
            return OptionalInt.empty();
        }
        return OptionalInt.of(SUBSEQUENCE - Math.min(gaps * GAP_PENALTY, MAX_GAP_PENALTY) - lengthPenalty);
    }

    private static boolean isWordStart(String text, int index) {
        return index == 0 || !Character.isLetterOrDigit(text.charAt(index - 1));
    }

    private static int subsequenceGaps(String needle, String haystack) {
        int gaps = 0;
        int position = 0;
        int previousMatch = -1;
        for (int i = 0; i < needle.length(); i++) {
            int found = haystack.indexOf(needle.charAt(i), position);
            if (found < 0) {
                return -1;
            }
            if (previousMatch >= 0) {
                gaps += found - previousMatch - 1;
            }
            previousMatch = found;
            position = found + 1;
        }
        return gaps;
    }

    private static String normalise(String value) {
        return value.strip().toLowerCase(Locale.ROOT);
    }
}
