package dev.dutchy.runelite.gear.ui;

import dev.dutchy.runelite.gear.SectionId;
import dev.dutchy.runelite.gear.SetupId;
import lombok.Value;
import lombok.experimental.Accessors;

import java.util.Objects;

/** A section or a setup picked for bulk deletion. */
public interface BulkTarget {

    static BulkTarget of(SectionId id) {
        return new Section(id);
    }

    static BulkTarget of(SetupId id) {
        return new Setup(id);
    }

    @Value
    @Accessors(fluent = true)
    class Section implements BulkTarget {
        SectionId id;


        public Section(SectionId id) {
            Objects.requireNonNull(id, "id");
            this.id = id;
        }
    }

    @Value
    @Accessors(fluent = true)
    class Setup implements BulkTarget {
        SetupId id;


        public Setup(SetupId id) {
            Objects.requireNonNull(id, "id");
            this.id = id;
        }
    }
}
