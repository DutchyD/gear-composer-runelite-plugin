package dev.dutchy.runelite.gear.persistence;

import java.util.stream.Collectors;
import com.google.gson.Gson;
import dev.dutchy.runelite.gear.GearSection;
import dev.dutchy.runelite.gear.GearSetupBook;
import dev.dutchy.runelite.gear.SectionId;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Delayed;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BookPersistenceTest {

    /** Remembers what was written and what was set aside. */
    private static final class MemoryStore implements BookStore {

        String document;
        final List<String> quarantined = new ArrayList<>();
        int writes;

        @Override
        public Optional<String> read() {
            return Optional.ofNullable(document);
        }

        @Override
        public void write(String encoded) {
            document = encoded;
            writes++;
        }

        @Override
        public void quarantine(String encoded) {
            quarantined.add(encoded);
        }
    }

    /** Runs scheduled work only when asked, so timing is deterministic. */
    @SuppressWarnings("NullableProblems")
    private static final class ManualScheduler extends ScheduledThreadPoolExecutor {

        private final List<Runnable> queued = new ArrayList<>();

        ManualScheduler() {
            super(1);
        }

        @Override
        public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
            queued.add(command);
            return new ScheduledFuture<>() {
                private boolean cancelled;

                @Override
                public long getDelay(TimeUnit timeUnit) {
                    return 0;
                }

                @Override
                public int compareTo(Delayed other) {
                    return 0;
                }

                @Override
                public boolean cancel(boolean mayInterrupt) {
                    cancelled = true;
                    return queued.remove(command);
                }

                @Override
                public boolean isCancelled() {
                    return cancelled;
                }

                @Override
                public boolean isDone() {
                    return cancelled;
                }

                @Override
                public Object get() {
                    return null;
                }

                @Override
                public Object get(long timeout, TimeUnit timeUnit) {
                    return null;
                }
            };
        }

        void runQueued() {
            List<Runnable> now = new ArrayList<>(queued);
            queued.clear();
            now.forEach(Runnable::run);
        }

        int queuedCount() {
            return queued.size();
        }
    }

    private final MemoryStore store = new MemoryStore();
    private final BookCodec codec = new BookCodec(new Gson());
    private final ManualScheduler scheduler = new ManualScheduler();
    private final BookPersistence persistence = new BookPersistence(store, codec, scheduler);

    @Test
    void nothingStoredLeavesTheBookAlone() {
        GearSetupBook book = new GearSetupBook();
        assertFalse(persistence.restore(book));
        assertEquals(1, book.sectionCount());
        assertEquals(GearSetupBook.DEFAULT_SECTION_NAME, book.sections().get(0).name());
    }

    @Test
    void changesAreSavedOnceAfterTheLastEdit() {
        GearSetupBook book = new GearSetupBook();
        AutoSave autoSave = persistence.autoSave(book);

        SectionId section = book.sections().get(0).id();
        book.addSetup(section, "Vorkath");
        book.addSetup(section, "Zulrah");
        assertEquals(1, scheduler.queuedCount(), "edits in quick succession share one pending save");
        assertEquals(0, store.writes);

        scheduler.runQueued();
        assertEquals(1, store.writes);

        GearSetupBook reloaded = new GearSetupBook();
        assertTrue(persistence.restore(reloaded));
        assertEquals(book.sections(), reloaded.sections());
        autoSave.stop();
    }

    @Test
    void stoppingHandsTheLastWriteToTheScheduler() {
        GearSetupBook book = new GearSetupBook();
        AutoSave autoSave = persistence.autoSave(book);
        book.addSetup(book.sections().get(0).id(), "Vorkath");

        autoSave.stop();

        assertEquals(0, store.writes, "shutdown must not reach the disk on the calling thread");
        assertEquals(1, scheduler.queuedCount(), "the pending save is waiting on the scheduler");

        scheduler.runQueued();

        assertEquals(1, store.writes);
        assertFalse(autoSave.hasUnsavedChanges());
        book.addSetup(book.sections().get(0).id(), "After");
        assertEquals(0, scheduler.queuedCount(), "a stopped auto-save no longer listens");
    }

    @Test
    void stoppingWithNothingPendingQueuesNoWrite() {
        GearSetupBook book = new GearSetupBook();
        AutoSave autoSave = persistence.autoSave(book);

        autoSave.stop();

        assertEquals(0, scheduler.queuedCount());
        assertEquals(0, store.writes);
    }

    @Test
    void anUnreadableDocumentIsSetAsideRatherThanOverwritten() {
        store.document = "{\"version\":42}";
        GearSetupBook book = new GearSetupBook();

        assertFalse(persistence.restore(book));

        assertEquals(List.of("{\"version\":42}"), store.quarantined);
        assertEquals(1, book.sectionCount());
    }

    @Test
    void restoringReplacesTheDefaultSection() {
        GearSetupBook source = new GearSetupBook("Bossing");
        source.addSetup(source.sections().get(0).id(), "Vorkath");
        store.document = codec.encode(source.sections());

        GearSetupBook book = new GearSetupBook();
        assertTrue(persistence.restore(book));
        assertEquals(List.of("Bossing"), book.sections().stream().map(GearSection::name).collect(Collectors.toList()));
        assertEquals(1, book.setupCount());
    }
}
