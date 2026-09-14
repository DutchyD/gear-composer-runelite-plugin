package dev.dutchy.runelite.gear.guide;

import java.util.EnumSet;
import java.util.Set;

/** Which guides have been run to their last step. */
public interface GuideProgress {

    Set<GuideId> completed();

    void markCompleted(GuideId id);

    default boolean isCompleted(GuideId id) {
        return completed().contains(id);
    }

    static GuideProgress inMemory() {
        return new GuideProgress() {
            private final Set<GuideId> done = EnumSet.noneOf(GuideId.class);

            @Override
            public Set<GuideId> completed() {
                return Set.copyOf(done);
            }

            @Override
            public void markCompleted(GuideId id) {
                done.add(id);
            }
        };
    }
}
