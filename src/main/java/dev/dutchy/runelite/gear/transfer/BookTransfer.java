package dev.dutchy.runelite.gear.transfer;

import dev.dutchy.runelite.gear.GearSection;
import dev.dutchy.runelite.gear.persistence.*;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

/** Everything that moves whole books in and out: files the player picks and the automatic backups. */
public final class BookTransfer {

    private final BookFiles files;
    private final BackupStore backups;
    private final BookCodec codec;

    @Inject
    public BookTransfer(BookFiles files, BackupStore backups, BookCodec codec) {
        this.files = Objects.requireNonNull(files, "files");
        this.backups = Objects.requireNonNull(backups, "backups");
        this.codec = Objects.requireNonNull(codec, "codec");
    }

    public void exportTo(Path file, List<GearSection> sections) throws IOException {
        files.export(file, sections);
    }

    /** @throws BookFormatException when the file is not a book */
    public List<GearSection> importFrom(Path file) throws IOException {
        return files.read(file);
    }

    public List<Backup> backups() {
        return backups.list();
    }

    /** @throws BookFormatException when the backup cannot be read */
    public List<GearSection> readBackup(Backup backup) {
        return codec.decode(backups.read(backup));
    }
}
