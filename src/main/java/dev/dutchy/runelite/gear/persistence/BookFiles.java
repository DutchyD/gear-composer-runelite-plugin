package dev.dutchy.runelite.gear.persistence;

import dev.dutchy.runelite.gear.GearSection;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

/** Reads and writes the book as a JSON file the player chooses. */
@Singleton
public final class BookFiles {

    public static final String EXTENSION = "json";

    private final BookCodec codec;

    @Inject
    public BookFiles(BookCodec codec) {
        this.codec = Objects.requireNonNull(codec, "codec");
    }

    public void export(Path file, List<GearSection> sections) throws IOException {
        Objects.requireNonNull(file, "file");
        Files.writeString(file, codec.encode(sections), StandardCharsets.UTF_8);
    }

    /** @throws BookFormatException when the file is not a book this version understands */
    public List<GearSection> read(Path file) throws IOException {
        Objects.requireNonNull(file, "file");
        return codec.decode(Files.readString(file, StandardCharsets.UTF_8));
    }
}
