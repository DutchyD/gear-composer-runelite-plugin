package dev.dutchy.runelite.gear.guide;

import dev.dutchy.runelite.gear.content.SetupType;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Value;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/** A titled run of steps; a tutorial also names the layout type its sample is made of. */
@Value
@Accessors(fluent = true)
public class Guide {
    GuideId id;
    List<GuideStep> steps;
    @Getter(AccessLevel.NONE)
    SetupType sampleType;

    public Guide(GuideId id, List<GuideStep> steps, SetupType sampleType) {
        this.id = Objects.requireNonNull(id, "id");
        this.steps = List.copyOf(Objects.requireNonNull(steps, "steps"));
        if (this.steps.isEmpty()) {
            throw new IllegalArgumentException("A guide needs at least one step");
        }
        this.sampleType = sampleType;
    }

    public static Guide of(GuideId id, List<GuideStep> steps) {
        return new Guide(id, steps, null);
    }

    public static Guide tutorial(GuideId id, SetupType sampleType, List<GuideStep> steps) {
        return new Guide(id, steps, Objects.requireNonNull(sampleType, "sampleType"));
    }

    public String title() {
        return id.title();
    }

    public Optional<SetupType> sampleType() {
        return Optional.ofNullable(sampleType);
    }

    public boolean needsSample() {
        return sampleType != null;
    }

    public int length() {
        return steps.size();
    }
}
