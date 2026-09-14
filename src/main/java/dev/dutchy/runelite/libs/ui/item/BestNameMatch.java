package dev.dutchy.runelite.libs.ui.item;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public final class BestNameMatch implements ItemNameMatcher {

    private static final int EXACT = 0;
    private static final int PREFIX = 1;
    private static final int CONTAINS = 2;
    private static final int NO_MATCH = 3;

    @Override
    public Optional<ResolvedItem> bestMatch(String query, List<ResolvedItem> candidates) {
        String needle = normalise(query);
        Comparator<ResolvedItem> ranking = Comparator
                .comparingInt((ResolvedItem item) -> rank(needle, normalise(item.name())))
                .thenComparingInt(item -> item.name().length())
                .thenComparing(ResolvedItem::id);

        return candidates.stream()
                .filter(item -> rank(needle, normalise(item.name())) != NO_MATCH)
                .min(ranking);
    }

    private static int rank(String needle, String haystack) {
        if (haystack.equals(needle)) {
            return EXACT;
        }
        if (haystack.startsWith(needle)) {
            return PREFIX;
        }
        if (haystack.contains(needle)) {
            return CONTAINS;
        }
        return NO_MATCH;
    }

    private static String normalise(String value) {
        return value.strip().toLowerCase(Locale.ROOT);
    }
}
