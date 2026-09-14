package dev.dutchy.runelite.gear.persistence;

import lombok.Value;
import lombok.experimental.Accessors;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Objects;

/** A saved copy of the book on disk. */
@Value
@Accessors(fluent = true)
public class Backup {
    Path file;
    Instant at;

    public Backup(Path file, Instant at) {
        Objects.requireNonNull(file, "file");
        Objects.requireNonNull(at, "at");
        this.file = file;
        this.at = at;
    }
}
