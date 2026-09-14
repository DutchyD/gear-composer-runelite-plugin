package dev.dutchy.runelite.gear.history;

import dev.dutchy.runelite.gear.SetupId;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class InMemoryHistoryStore implements HistoryStore {

    private final Map<SetupId, List<SetupRevision>> revisions = new HashMap<>();

    @Override
    public List<SetupRevision> revisions(SetupId setup) {
        return List.copyOf(revisions.getOrDefault(Objects.requireNonNull(setup, "setup"), List.of()));
    }

    @Override
    public void record(SetupId setup, SetupRevision revision) {
        Objects.requireNonNull(revision, "revision");
        revisions.put(Objects.requireNonNull(setup, "setup"), RevisionTrail.with(revisions(setup), revision));
    }

    @Override
    public void forget(SetupId setup) {
        revisions.remove(Objects.requireNonNull(setup, "setup"));
    }
}
