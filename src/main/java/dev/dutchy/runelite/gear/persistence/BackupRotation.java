package dev.dutchy.runelite.gear.persistence;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

/** Keeps the newest few backups plus one per day for a while, and lets the rest go. */
public final class BackupRotation {

    private final int keepNewest;
    private final Duration keepDaily;

    public BackupRotation(int keepNewest, Duration keepDaily) {
        if (keepNewest < 1) {
            throw new IllegalArgumentException("Keep at least one backup");
        }
        this.keepNewest = keepNewest;
        this.keepDaily = Objects.requireNonNull(keepDaily, "keepDaily");
    }

    public static BackupRotation standard() {
        return new BackupRotation(20, Duration.ofDays(30));
    }

    /** Which of the given backup times survive, judged at {@code now}. */
    public Set<Instant> keep(List<Instant> backups, Instant now) {
        Objects.requireNonNull(backups, "backups");
        Objects.requireNonNull(now, "now");
        List<Instant> newestFirst = backups.stream().sorted(Comparator.reverseOrder()).collect(Collectors.toList());
        Set<Instant> kept = new HashSet<>(newestFirst.subList(0, Math.min(keepNewest, newestFirst.size())));
        Set<LocalDate> daysCovered = new HashSet<>();
        Instant horizon = now.minus(keepDaily);
        for (Instant backup : newestFirst) {
            if (backup.isBefore(horizon)) {
                break;
            }
            if (daysCovered.add(LocalDate.ofInstant(backup, ZoneOffset.UTC))) {
                kept.add(backup);
            }
        }
        return kept;
    }
}
