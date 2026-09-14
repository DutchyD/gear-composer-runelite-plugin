package dev.dutchy.runelite.gear.persistence;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BackupRotationTest {

    private static final Instant NOW = Instant.parse("2026-09-08T12:00:00Z");

    @Test
    void theNewestBackupsAlwaysSurvive() {
        List<Instant> backups = new ArrayList<>();
        for (int minutes = 0; minutes < 10; minutes++) {
            backups.add(NOW.minus(Duration.ofMinutes(minutes)));
        }
        Set<Instant> kept = new BackupRotation(3, Duration.ofDays(30)).keep(backups, NOW);
        assertTrue(kept.containsAll(backups.subList(0, 3)));
        assertEquals(3, kept.size(), "same day, so the daily rule adds nothing new");
    }

    @Test
    void onePerDayIsKeptWithinTheWindowAndNothingOlder() {
        List<Instant> backups = List.of(
                NOW,
                NOW.minus(Duration.ofDays(1)),
                NOW.minus(Duration.ofDays(1)).minus(Duration.ofHours(1)),
                NOW.minus(Duration.ofDays(29)),
                NOW.minus(Duration.ofDays(31)));
        Set<Instant> kept = new BackupRotation(1, Duration.ofDays(30)).keep(backups, NOW);
        assertTrue(kept.contains(NOW));
        assertTrue(kept.contains(NOW.minus(Duration.ofDays(1))), "the newest of that day");
        assertFalse(kept.contains(NOW.minus(Duration.ofDays(1)).minus(Duration.ofHours(1))));
        assertTrue(kept.contains(NOW.minus(Duration.ofDays(29))));
        assertFalse(kept.contains(NOW.minus(Duration.ofDays(31))));
    }
}
