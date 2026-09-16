package dev.dutchy.runelite.gear.ui;

import dev.dutchy.runelite.gear.content.CustomContent;
import dev.dutchy.runelite.gear.content.GearContent;
import dev.dutchy.runelite.gear.content.SetupContent;

/** What a new variant starts out holding. */
public enum NewVariant {
    COPY("Copy of the shown variant"),
    EMPTY("Empty"),
    FROM_GAME("From what you wear and carry");

    private final String label;

    NewVariant(String label) {
        this.label = label;
    }

    String label() {
        return label;
    }

    /** Whether the choice makes sense for the content; only gear can be taken from the game. */
    boolean offeredFor(SetupContent content) {
        return this != FROM_GAME || content instanceof GearContent;
    }

    /** An empty start that keeps a custom layout's row count. */
    static SetupContent emptyLike(SetupContent shown) {
        return shown instanceof CustomContent ? CustomContent.empty(((CustomContent) shown).rows()) : SetupContent.empty(shown.type());
    }
}
