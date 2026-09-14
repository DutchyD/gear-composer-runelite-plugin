package dev.dutchy.runelite.gear;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Value;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/** A named run of setups; a section may sit one level under another, read through {@link #parent()}. */
@Value
@Accessors(fluent = true)
public class GearSection {
    SectionId id;
    String name;
    List<GearSetup> setups;
    @Getter(AccessLevel.NONE)
    SectionId parentId;

    public static final int MAX_NAME_LENGTH = 48;

    public GearSection(SectionId id, String name, List<GearSetup> setups) {
        this(id, name, setups, null);
    }

    public GearSection(SectionId id, String name, List<GearSetup> setups, SectionId parentId) {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(setups, "setups");
        if (id.equals(parentId)) {
            throw new IllegalArgumentException("A section cannot be its own parent");
        }
        this.id = id;
        this.name = requireValidName(name);
        this.setups = List.copyOf(setups);
        this.parentId = parentId;
    }

    public static GearSection named(String name) {
        return new GearSection(SectionId.random(), name, List.of());
    }

    public Optional<SectionId> parent() {
        return Optional.ofNullable(parentId);
    }

    public boolean isChild() {
        return parentId != null;
    }

    public boolean isChildOf(SectionId candidate) {
        return candidate.equals(parentId);
    }

    public GearSection withName(String newName) {
        return new GearSection(id, newName, setups, parentId);
    }

    public GearSection withSetups(List<GearSetup> newSetups) {
        return new GearSection(id, name, newSetups, parentId);
    }

    public GearSection withParent(SectionId newParent) {
        return new GearSection(id, name, setups, Objects.requireNonNull(newParent, "newParent"));
    }

    public GearSection withoutParent() {
        return new GearSection(id, name, setups, null);
    }

    public boolean isEmpty() {
        return setups.isEmpty();
    }

    public int size() {
        return setups.size();
    }

    public static String requireValidName(String name) {
        Objects.requireNonNull(name, "name");
        String trimmed = name.strip();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("Section name must not be blank");
        }
        if (trimmed.length() > MAX_NAME_LENGTH) {
            throw new IllegalArgumentException("Section name must be at most " + MAX_NAME_LENGTH + " characters");
        }
        return trimmed;
    }

    public static boolean isValidName(String name) {
        if (name == null) {
            return false;
        }
        String trimmed = name.strip();
        return !trimmed.isEmpty() && trimmed.length() <= MAX_NAME_LENGTH;
    }
}
