package dev.dutchy.runelite.gear.persistence;

import java.nio.file.Path;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FileBackupStoreTest {

    @TempDir
    Path directory;

    /** A clock that moves forward a second per read, so every backup gets its own name. */
    private static final class TickingClock extends Clock {
        private Instant now = Instant.parse("2026-09-08T12:00:00Z");

        @Override
        public ZoneOffset getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            now = now.plusSeconds(1);
            return now;
        }
    }

    @Test
    void savesAreListedNewestFirstAndOldOnesAreThinned() {
        FileBackupStore store = new FileBackupStore(directory.resolve("backups"), new BackupRotation(2, Duration.ofDays(30)), new TickingClock());
        store.save("one");
        store.save("two");
        store.save("three");

        List<Backup> backups = store.list();
        assertEquals(2, backups.size());
        assertEquals("three", store.read(backups.get(0)));
        assertEquals("two", store.read(backups.get(1)));
    }

    @Test
    void aMissingDirectoryMeansNoBackups() {
        assertEquals(List.of(), new FileBackupStore(directory.resolve("none"), BackupRotation.standard(), Clock.systemUTC()).list());
    }
}
