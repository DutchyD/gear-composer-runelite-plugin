package dev.dutchy.runelite.gear;

import dev.dutchy.runelite.libs.ui.search.ItemNames;
import dev.dutchy.runelite.libs.ui.search.NameScorer;

import javax.inject.Inject;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public final class SetupFilter {

    /** A query such as "tag:melee" matches on tags alone. */
    public static final String TAG_PREFIX = "tag:";

    private final NameScorer scorer;

    @Inject
    public SetupFilter(NameScorer scorer) {
        this.scorer = Objects.requireNonNull(scorer, "scorer");
    }

    /** Blank query returns every section. Match order is preserved, not ranked. */
    public List<GearSection> apply(List<GearSection> sections, String query) {
        Objects.requireNonNull(sections, "sections");
        Objects.requireNonNull(query, "query");
        String needle = ItemNames.normalize(query);
        if (needle.isEmpty()) {
            return List.copyOf(sections);
        }
        return sections.stream()
                .map(section -> section.withSetups(section.setups().stream()
                        .filter(setup -> matches(needle, setup))
                        .collect(Collectors.toList())))
                .filter(section -> !section.isEmpty())
                .collect(Collectors.toList());
    }

    private boolean matches(String normalizedQuery, GearSetup setup) {
        if (normalizedQuery.startsWith(TAG_PREFIX)) {
            String tag = normalizedQuery.substring(TAG_PREFIX.length()).strip();
            return !tag.isEmpty() && setup.meta().tags().stream().anyMatch(candidate -> candidate.contains(tag));
        }
        return scorer.score(normalizedQuery, ItemNames.normalize(setup.name())).isPresent()
                || setup.meta().tags().stream().anyMatch(tag -> tag.contains(normalizedQuery));
    }
}
