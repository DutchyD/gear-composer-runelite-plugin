package dev.dutchy.runelite.gear.content;

import lombok.Value;

import java.util.Objects;

/**
 * One named version of a setup's contents, such as Budget or Best-in-slot. The id is what points at
 * a variant, so a pointer survives the variant being renamed or moved.
 */
@Value
public class SetupVariant {
    VariantId id;
    String name;
    SetupContent content;

    public static final String DEFAULT_NAME = "Default";
    public static final int MAX_NAME_LENGTH = 24;

    public SetupVariant(String name, SetupContent content) {
        this(VariantId.random(), name, content);
    }

    public SetupVariant(VariantId id, String name, SetupContent content) {
        this.id = Objects.requireNonNull(id, "id");
        this.name = requireValidName(name);
        this.content = Objects.requireNonNull(content, "content");
    }

    public static SetupVariant of(SetupContent content) {
        return new SetupVariant(DEFAULT_NAME, content);
    }

    public boolean hasId(VariantId other) {
        return id.equals(other);
    }

    /** The same variant under another id, as when a shared setup is taken in. */
    public SetupVariant withId(VariantId newId) {
        return new SetupVariant(newId, name, content);
    }

    public SetupType type() {
        return content.type();
    }

    public SetupVariant withName(String newName) {
        return new SetupVariant(id, newName, content);
    }

    public SetupVariant withContent(SetupContent newContent) {
        return new SetupVariant(id, name, newContent);
    }

    public boolean isNamed(String other) {
        return other != null && name.equalsIgnoreCase(other.strip());
    }

    public static String requireValidName(String name) {
        Objects.requireNonNull(name, "name");
        String trimmed = name.strip();
        if (trimmed.isEmpty() || trimmed.length() > MAX_NAME_LENGTH) {
            throw new IllegalArgumentException("Variant name must be 1 to " + MAX_NAME_LENGTH + " characters");
        }
        return trimmed;
    }

    public static boolean isValidName(String name) {
        return name != null && !name.isBlank() && name.strip().length() <= MAX_NAME_LENGTH;
    }
}
