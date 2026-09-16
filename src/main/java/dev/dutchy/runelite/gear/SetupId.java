package dev.dutchy.runelite.gear;

import lombok.Value;
import java.util.Objects;
import java.util.UUID;

@Value
public class SetupId {
    UUID value;

    public SetupId(UUID value) {
        Objects.requireNonNull(value, "value");
        this.value = value;
    }

    public static SetupId random() {
        return new SetupId(UUID.randomUUID());
    }

    @Override
    public String toString() {
        return "setup:" + value;
    }
}
