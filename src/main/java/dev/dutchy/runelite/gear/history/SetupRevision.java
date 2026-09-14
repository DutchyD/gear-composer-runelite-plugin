package dev.dutchy.runelite.gear.history;

import dev.dutchy.runelite.gear.content.SetupContent;
import dev.dutchy.runelite.gear.content.VariantId;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Value;
import lombok.experimental.Accessors;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

/** What one variant's items looked like before a change, and what caused the change. */
@Value
@Accessors(fluent = true)
public class SetupRevision {
    Instant at;
    String cause;
    @Getter(AccessLevel.NONE)
    VariantId variantId;
    SetupContent content;

    public static final int MAX_CAUSE = 60;

    /** A revision from before variants carried ids, so it does not know which one it came from. */
    public SetupRevision(Instant at, String cause, SetupContent content) {
        this(at, cause, null, content);
    }

    /** The variant this came from, empty for a revision recorded before variants had ids. */
    public Optional<VariantId> variant() {
        return Optional.ofNullable(variantId);
    }

    public SetupRevision(Instant at, String cause, VariantId variantId, SetupContent content) {
        Objects.requireNonNull(at, "at");
        Objects.requireNonNull(content, "content");
        cause = Objects.requireNonNull(cause, "cause").strip();
        if (cause.isEmpty() || cause.length() > MAX_CAUSE) {
            throw new IllegalArgumentException("A cause must be 1 to " + MAX_CAUSE + " characters");
        }
        this.at = at;
        this.cause = cause;
        this.variantId = variantId;
        this.content = content;
    }
}
