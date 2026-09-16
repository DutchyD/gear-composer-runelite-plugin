package dev.dutchy.runelite.gear.persistence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** Backups as {@code book-<timestamp>.json} files in one directory, thinned by a {@link BackupRotation}. */
public final class FileBackupStore implements BackupStore {

    private static final Logger log = LoggerFactory.getLogger(FileBackupStore.class);
    private static final String PREFIX = "book-";
    private static final String SUFFIX = ".json";
    private static final DateTimeFormatter STAMP = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss-SSS").withZone(ZoneOffset.UTC);

    private final Path directory;
    private final BackupRotation rotation;
    private final Clock clock;

    public FileBackupStore(Path directory, BackupRotation rotation, Clock clock) {
        this.directory = Objects.requireNonNull(directory, "directory");
        this.rotation = Objects.requireNonNull(rotation, "rotation");
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    @Override
    public void save(String encoded) {
        Objects.requireNonNull(encoded, "encoded");
        Instant now = clock.instant();
        try {
            Files.createDirectories(directory);
            Path temporary = Files.createTempFile(directory, "backup", ".tmp");
            Files.writeString(temporary, encoded, StandardCharsets.UTF_8);
            Files.move(temporary, fileFor(now), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            rotate(now);
        } catch (IOException e) {
            log.warn("Backup could not be written", e);
        }
    }

    @Override
    public List<Backup> list() {
        if (!Files.isDirectory(directory)) {
            return List.of();
        }
        try (Stream<Path> files = Files.list(directory)) {
            List<Backup> backups = new ArrayList<>();
            files.forEach(file -> parse(file).ifPresent(at -> backups.add(new Backup(file, at))));
            backups.sort(Comparator.comparing(Backup::at).reversed());
            return List.copyOf(backups);
        } catch (IOException e) {
            log.warn("Backups could not be listed", e);
            return List.of();
        }
    }

    @Override
    public String read(Backup backup) {
        try {
            return Files.readString(Objects.requireNonNull(backup, "backup").file(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void rotate(Instant now) {
        List<Backup> backups = list();
        Set<Instant> kept = rotation.keep(backups.stream().map(Backup::at).collect(Collectors.toList()), now);
        for (Backup backup : backups) {
            if (!kept.contains(backup.at())) {
                try {
                    Files.deleteIfExists(backup.file());
                } catch (IOException e) {
                    log.warn("Old backup {} could not be removed", backup.file(), e);
                }
            }
        }
    }

    private Path fileFor(Instant at) {
        return directory.resolve(PREFIX + STAMP.format(at) + SUFFIX);
    }

    private static Optional<Instant> parse(Path file) {
        String name = file.getFileName().toString();
        if (!name.startsWith(PREFIX) || !name.endsWith(SUFFIX)) {
            return Optional.empty();
        }
        String stamp = name.substring(PREFIX.length(), name.length() - SUFFIX.length());
        try {
            return Optional.of(Instant.from(STAMP.parse(stamp)));
        } catch (DateTimeParseException e) {
            return Optional.empty();
        }
    }
}
