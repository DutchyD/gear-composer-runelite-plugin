package dev.dutchy.runelite.gear.history;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * How a setup's trail of revisions grows. Each variant keeps its own last
 * {@link HistoryStore#MAX_REVISIONS}, so editing one variant cannot push another's history out.
 */
final class RevisionTrail {

    private RevisionTrail() {
    }

    /** The trail with the revision at its head, newest first, trimmed to what its variant may keep. */
    static List<SetupRevision> with(List<SetupRevision> trail, SetupRevision revision) {
        Objects.requireNonNull(trail, "trail");
        Objects.requireNonNull(revision, "revision");
        List<SetupRevision> updated = new ArrayList<>(trail);
        updated.add(0, revision);
        int kept = 0;
        for (Iterator<SetupRevision> each = updated.iterator(); each.hasNext(); ) {
            if (each.next().variant().equals(revision.variant()) && ++kept > HistoryStore.MAX_REVISIONS) {
                each.remove();
            }
        }
        return List.copyOf(updated);
    }
}
