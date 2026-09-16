package dev.dutchy.runelite.gear.persistence;

import java.util.Objects;
import java.util.Optional;

/** Writes through to the real store and drops a backup copy on every write. */
public final class BackedUpBookStore implements BookStore {

    private final BookStore delegate;
    private final BackupStore backups;

    public BackedUpBookStore(BookStore delegate, BackupStore backups) {
        this.delegate = Objects.requireNonNull(delegate, "delegate");
        this.backups = Objects.requireNonNull(backups, "backups");
    }

    @Override
    public Optional<String> read() {
        return delegate.read();
    }

    @Override
    public void write(String encoded) {
        delegate.write(encoded);
        backups.save(encoded);
    }

    @Override
    public void quarantine(String encoded) {
        delegate.quarantine(encoded);
    }
}
