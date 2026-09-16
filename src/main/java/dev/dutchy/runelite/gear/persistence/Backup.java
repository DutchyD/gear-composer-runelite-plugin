package dev.dutchy.runelite.gear.persistence;

import lombok.Value;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Objects;

/** A saved copy of the book on disk. */
@Value
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
