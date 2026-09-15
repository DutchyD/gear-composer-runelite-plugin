package dev.dutchy.runelite.gear.content;

import lombok.Value;

import java.util.Objects;
import java.util.UUID;

/** A variant's stable identity, so a pointer to it survives reordering, renaming and deletion. */
@Value
public class VariantId {
    UUID value;

    public VariantId(UUID value) {
        Objects.requireNonNull(value, "value");
        this.value = value;
    }

    public static VariantId random() {
        return new VariantId(UUID.randomUUID());
    }

    @Override
    public String toString() {
        return "variant:" + value;
    }
}
