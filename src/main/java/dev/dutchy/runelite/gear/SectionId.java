package dev.dutchy.runelite.gear;

import lombok.Value;

import java.util.Objects;
import java.util.UUID;

@Value
public class SectionId {
    UUID value;

    public SectionId(UUID value) {
        Objects.requireNonNull(value, "value");
        this.value = value;
    }

    public static SectionId random() {
        return new SectionId(UUID.randomUUID());
    }

    @Override
    public String toString() {
        return "section:" + value;
    }
}
