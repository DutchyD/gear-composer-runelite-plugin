package dev.dutchy.runelite.gear;

import lombok.Value;

import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/** Everything about a setup that is not its items: pinning, colour, tags, notes, requirements, hotkey. */
@Value
public class SetupMeta {
    boolean pinned;
    ColourLabel label;
    Set<String> tags;
    String notes;
    Requirements requirements;
    Hotkey hotkey;

    public static final int MAX_TAGS = 8;
    public static final int MAX_TAG_LENGTH = 24;
    public static final int MAX_NOTES = 500;

    private static final SetupMeta NONE = new SetupMeta(false, ColourLabel.NONE, Set.of(), "", Requirements.none(), Hotkey.NONE);

    public SetupMeta(boolean pinned, ColourLabel label, Set<String> tags, String notes, Requirements requirements, Hotkey hotkey) {
        Objects.requireNonNull(label, "label");
        Objects.requireNonNull(tags, "tags");
        Objects.requireNonNull(requirements, "requirements");
        Objects.requireNonNull(hotkey, "hotkey");
        notes = Objects.requireNonNull(notes, "notes").strip();
        if (notes.length() > MAX_NOTES) {
            throw new IllegalArgumentException("Notes must be at most " + MAX_NOTES + " characters");
        }
        tags = normalizeTags(tags);
        this.pinned = pinned;
        this.label = label;
        this.tags = tags;
        this.notes = notes;
        this.requirements = requirements;
        this.hotkey = hotkey;
    }

    public static SetupMeta none() {
        return NONE;
    }

    public SetupMeta withPinned(boolean isPinned) {
        return new SetupMeta(isPinned, label, tags, notes, requirements, hotkey);
    }

    public SetupMeta withLabel(ColourLabel newLabel) {
        return new SetupMeta(pinned, newLabel, tags, notes, requirements, hotkey);
    }

    public SetupMeta withTags(Set<String> newTags) {
        return new SetupMeta(pinned, label, newTags, notes, requirements, hotkey);
    }

    public SetupMeta withNotes(String newNotes) {
        return new SetupMeta(pinned, label, tags, newNotes, requirements, hotkey);
    }

    public SetupMeta withRequirements(Requirements newRequirements) {
        return new SetupMeta(pinned, label, tags, notes, newRequirements, hotkey);
    }

    public SetupMeta withHotkey(Hotkey newHotkey) {
        return new SetupMeta(pinned, label, tags, notes, requirements, newHotkey);
    }

    public boolean hasHotkey() {
        return hotkey.isSet();
    }

    public boolean hasNotes() {
        return !notes.isEmpty();
    }

    /** Lower-cased, trimmed, sorted, and capped; blank tags are dropped. */
    public static Set<String> normalizeTags(Set<String> raw) {
        Set<String> normalized = new TreeSet<>();
        for (String tag : raw) {
            String cleaned = Objects.requireNonNull(tag, "tag").strip().toLowerCase(Locale.ROOT);
            if (cleaned.isEmpty()) {
                continue;
            }
            if (cleaned.length() > MAX_TAG_LENGTH) {
                throw new IllegalArgumentException("A tag must be at most " + MAX_TAG_LENGTH + " characters");
            }
            normalized.add(cleaned);
        }
        if (normalized.size() > MAX_TAGS) {
            throw new IllegalArgumentException("At most " + MAX_TAGS + " tags are allowed");
        }
        return Set.copyOf(normalized).stream().sorted().collect(Collectors.toCollection(TreeSet::new));
    }
}
