package dev.dutchy.runelite.libs.ui.search;

import java.util.OptionalInt;

@FunctionalInterface
public interface NameScorer {

    /** Higher is better. Empty means no match. */
    OptionalInt score(String query, String candidateName);
}
