package dev.dutchy.runelite.gear.persistence;

import dev.dutchy.runelite.gear.GearSection;
import dev.dutchy.runelite.gear.GearSetupBook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;

/** Restores the book at start-up and keeps it saved afterwards. */
@Singleton
public final class BookPersistence {

    static final Duration SAVE_DELAY = Duration.ofMillis(500);

    private static final Logger log = LoggerFactory.getLogger(BookPersistence.class);

    private final BookStore store;
    private final BookCodec codec;
    private final ScheduledExecutorService scheduler;

    @Inject
    public BookPersistence(BookStore store, BookCodec codec, ScheduledExecutorService scheduler) {
        this.store = Objects.requireNonNull(store, "store");
        this.codec = Objects.requireNonNull(codec, "codec");
        this.scheduler = Objects.requireNonNull(scheduler, "scheduler");
    }

    /** Fills the book from storage. An unreadable document is quarantined and the book is left as it was. */
    public boolean restore(GearSetupBook book) {
        Objects.requireNonNull(book, "book");
        Optional<String> stored = store.read();
        if (stored.isEmpty()) {
            return false;
        }
        List<GearSection> sections;
        try {
            sections = codec.decode(stored.get());
        } catch (BookFormatException e) {
            log.warn("Stored gear setups could not be read and were set aside", e);
            store.quarantine(stored.get());
            return false;
        }
        if (sections.isEmpty()) {
            return false;
        }
        book.replaceSections(sections);
        return true;
    }

    /** Saves the book shortly after every change until {@link AutoSave#stop()} is called. */
    public AutoSave autoSave(GearSetupBook book) {
        AutoSave autoSave = new AutoSave(store, codec, scheduler, SAVE_DELAY);
        autoSave.watch(book);
        return autoSave;
    }
}
