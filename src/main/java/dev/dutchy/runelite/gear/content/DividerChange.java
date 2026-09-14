package dev.dutchy.runelite.gear.content;

import java.util.Locale;
import java.util.Objects;
import java.util.function.UnaryOperator;

/** The one-step changes a divider's menu offers, each knowing when it would make a difference. */
public enum DividerChange {
    TEXT_LEFT("Text left", divider -> divider.withAlign(TextAlign.LEFT)),
    TEXT_CENTRE("Text centre", divider -> divider.withAlign(TextAlign.CENTRE)),
    TEXT_RIGHT("Text right", divider -> divider.withAlign(TextAlign.RIGHT)),
    UNDERLINE("Underline", divider -> divider.withUnderline(true)),
    NO_UNDERLINE("No underline", divider -> divider.withUnderline(false));

    private final String label;
    private final UnaryOperator<Divider> change;

    DividerChange(String label, UnaryOperator<Divider> change) {
        this.label = label;
        this.change = change;
    }

    public String label() {
        return label;
    }

    public Divider apply(Divider divider) {
        return change.apply(Objects.requireNonNull(divider, "divider"));
    }

    /** Whether applying it would change the divider. */
    public boolean changes(Divider divider) {
        return !apply(divider).equals(divider);
    }

    /** How the change reads in the history. */
    public String describe() {
        return "Divider: " + label.toLowerCase(Locale.ROOT);
    }
}
