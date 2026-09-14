package dev.dutchy.runelite.gear.persistence;

import dev.dutchy.runelite.gear.GearSection;
import dev.dutchy.runelite.gear.GearSetupBook;
import dev.dutchy.runelite.gear.GearSetupBookListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/** Writes the book a moment after it last changed, and immediately on {@link #flush()}. */
public final class AutoSave implements GearSetupBookListener {

    private static final Logger log = LoggerFactory.getLogger(AutoSave.class);

    private final BookStore store;
    private final BookCodec codec;
    private final ScheduledExecutorService scheduler;
    private final Duration delay;
    private final AtomicReference<List<GearSection>> pending = new AtomicReference<>();
    private final Object lock = new Object();

    private ScheduledFuture<?> scheduled;
    private GearSetupBook watched;

    public AutoSave(BookStore store, BookCodec codec, ScheduledExecutorService scheduler, Duration delay) {
        this.store = Objects.requireNonNull(store, "store");
        this.codec = Objects.requireNonNull(codec, "codec");
        this.scheduler = Objects.requireNonNull(scheduler, "scheduler");
        this.delay = Objects.requireNonNull(delay, "delay");
        if (delay.isNegative()) {
            throw new IllegalArgumentException("Delay must not be negative");
        }
    }

    public void watch(GearSetupBook book) {
        Objects.requireNonNull(book, "book");
        synchronized (lock) {
            if (watched != null) {
                throw new IllegalStateException("Already watching a book");
            }
            watched = book;
        }
        book.addChangeListener(this);
    }

    /**
     * Stops listening at once and hands any last write to the scheduler. The write does not happen
     * on the calling thread: this runs from the plugin's shutdown, which is the client thread, and
     * a write reaches the disk through the backup store. The scheduler is single threaded, so the
     * write still lands before anything a restart reads.
     */
    public void stop() {
        GearSetupBook book;
        synchronized (lock) {
            book = watched;
            watched = null;
        }
        if (book != null) {
            book.removeChangeListener(this);
        }
        synchronized (lock) {
            if (scheduled != null) {
                scheduled.cancel(false);
                scheduled = null;
            }
            if (pending.get() != null) {
                scheduled = scheduler.schedule(this::flush, 0, TimeUnit.MILLISECONDS);
            }
        }
    }

    @Override
    public void onBookChanged(GearSetupBook book) {
        pending.set(book.sections());
        synchronized (lock) {
            if (scheduled != null) {
                scheduled.cancel(false);
            }
            scheduled = scheduler.schedule(this::flush, delay.toMillis(), TimeUnit.MILLISECONDS);
        }
    }

    /** Writes whatever is waiting, now, on the calling thread. */
    public void flush() {
        synchronized (lock) {
            if (scheduled != null) {
                scheduled.cancel(false);
                scheduled = null;
            }
        }
        List<GearSection> sections = pending.getAndSet(null);
        if (sections == null) {
            return;
        }
        try {
            store.write(codec.encode(sections));
        } catch (RuntimeException e) {
            log.warn("Could not save gear setups", e);
            pending.compareAndSet(null, sections);
        }
    }

    public boolean hasUnsavedChanges() {
        return pending.get() != null;
    }
}
