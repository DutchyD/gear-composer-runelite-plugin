package dev.dutchy.runelite.gear.history;

import dev.dutchy.runelite.gear.SetupId;

import java.util.List;

/** Keeps a bounded trail of past contents per setup. Newest first. Safe to call from the EDT. */
public interface HistoryStore {

    int MAX_REVISIONS = 10;

    List<SetupRevision> revisions(SetupId setup);

    void record(SetupId setup, SetupRevision revision);

    void forget(SetupId setup);
}
