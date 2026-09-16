package dev.dutchy.runelite.gear.history;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import dev.dutchy.runelite.gear.SetupId;
import dev.dutchy.runelite.gear.content.VariantId;
import dev.dutchy.runelite.gear.persistence.ContentCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

/** One JSON file per setup under a directory; an unreadable file counts as no history. */
public final class FileHistoryStore implements HistoryStore {

    private static final Logger log = LoggerFactory.getLogger(FileHistoryStore.class);

    private final Path directory;
    private final Gson gson;

    public FileHistoryStore(Path directory, Gson gson) {
        this.directory = Objects.requireNonNull(directory, "directory");
        this.gson = Objects.requireNonNull(gson, "gson");
    }

    @Override
    public List<SetupRevision> revisions(SetupId setup) {
        Path file = fileFor(setup);
        if (!Files.exists(file)) {
            return List.of();
        }
        try {
            List<RevisionDto> dtos = gson.fromJson(Files.readString(file, StandardCharsets.UTF_8),
                    new TypeToken<List<RevisionDto>>() {
                    }.getType());
            List<SetupRevision> revisions = new ArrayList<>();
            for (RevisionDto dto : dtos == null ? List.<RevisionDto>of() : dtos) {
                revisions.add(new SetupRevision(Instant.ofEpochMilli(dto.at), dto.cause, variantOf(dto.variant),
                        ContentCodec.fromDto(dto.content)));
            }
            return List.copyOf(revisions);
        } catch (IOException | JsonParseException | IllegalArgumentException | NullPointerException e) {
            log.warn("History for {} could not be read", setup, e);
            return List.of();
        }
    }

    @Override
    public void record(SetupId setup, SetupRevision revision) {
        Objects.requireNonNull(revision, "revision");
        write(setup, RevisionTrail.with(revisions(setup), revision));
    }

    @Override
    public void forget(SetupId setup) {
        try {
            Files.deleteIfExists(fileFor(setup));
        } catch (IOException e) {
            log.warn("History for {} could not be removed", setup, e);
        }
    }

    private void write(SetupId setup, List<SetupRevision> trail) {
        List<RevisionDto> dtos = trail.stream().map(revision -> {
            RevisionDto dto = new RevisionDto();
            dto.at = revision.at().toEpochMilli();
            dto.cause = revision.cause();
            dto.variant = revision.variant().map(variant -> variant.value().toString()).orElse(null);
            dto.content = ContentCodec.toDto(revision.content());
            return dto;
        }).collect(Collectors.toList());
        try {
            Files.createDirectories(directory);
            Path temporary = Files.createTempFile(directory, "history", ".tmp");
            Files.writeString(temporary, gson.toJson(dtos), StandardCharsets.UTF_8);
            Files.move(temporary, fileFor(setup), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            log.warn("History for {} could not be written", setup, e);
        }
    }

    /** A revision written before variants had ids has none, and restores into whichever is shown. */
    private static VariantId variantOf(String written) {
        return written == null ? null : new VariantId(UUID.fromString(written));
    }

    private Path fileFor(SetupId setup) {
        return directory.resolve(Objects.requireNonNull(setup, "setup").value() + ".json");
    }

    private static final class RevisionDto {
        long at;
        String cause;
        String variant;
        ContentCodec.ContentDto content;
    }
}
