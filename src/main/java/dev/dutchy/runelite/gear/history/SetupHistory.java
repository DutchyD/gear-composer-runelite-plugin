package dev.dutchy.runelite.gear.history;

import dev.dutchy.runelite.gear.GearSetup;
import dev.dutchy.runelite.gear.GearSetupBook;
import dev.dutchy.runelite.gear.SetupId;
import dev.dutchy.runelite.gear.content.SetupContent;
import dev.dutchy.runelite.gear.content.VariantId;

import javax.inject.Inject;
import java.time.Clock;
import java.util.List;
import java.util.Objects;

/** Records a setup's previous items whenever its contents change through here. */
public final class SetupHistory {

    private final GearSetupBook book;
    private final HistoryStore store;
    private final Clock clock;

    @Inject
    public SetupHistory(GearSetupBook book, HistoryStore store, Clock clock) {
        this.book = Objects.requireNonNull(book, "book");
        this.store = Objects.requireNonNull(store, "store");
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    /** Replaces the selected variant's contents and remembers what they were, unless nothing changed. */
    public void applyContent(SetupId setup, SetupContent content, String cause) {
        applyContent(setup, found(setup).selected(), content, cause);
    }

    /** Replaces one variant's contents and remembers what they were, unless nothing changed. */
    public void applyContent(SetupId setup, VariantId variant, SetupContent content, String cause) {
        Objects.requireNonNull(content, "content");
        SetupContent before = found(setup).variant(variant)
                .orElseThrow(() -> new IllegalArgumentException("No variant " + variant)).content();
        if (before.equals(content)) {
            return;
        }
        store.record(setup, new SetupRevision(clock.instant(), cause, variant, before));
        book.updateVariantContent(setup, variant, content);
    }

    private GearSetup found(SetupId setup) {
        return book.setup(setup).orElseThrow(() -> new IllegalArgumentException("No setup " + setup));
    }

    public List<SetupRevision> revisions(SetupId setup) {
        return store.revisions(setup);
    }

    /**
     * Puts an old revision back into the variant it came from, recording the current contents first so the
     * restore itself can be undone. A revision from before variants had ids goes to the selected variant.
     */
    public void restore(SetupId setup, SetupRevision revision) {
        Objects.requireNonNull(revision, "revision");
        applyContent(setup, revision.variant().orElseGet(() -> found(setup).selected()),
                revision.content(), "Restored from history");
    }

    public void forget(SetupId setup) {
        store.forget(setup);
    }
}
