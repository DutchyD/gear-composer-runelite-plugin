package dev.dutchy.runelite.libs.ui.item;

import java.util.List;
import java.util.Optional;

@FunctionalInterface
public interface ItemNameMatcher {

    Optional<ResolvedItem> bestMatch(String query, List<ResolvedItem> candidates);
}
