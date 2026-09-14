package dev.dutchy.runelite.gear.bank;

import lombok.Value;
import lombok.experimental.Accessors;

import java.util.Objects;

/**
 * One widget child's finished appearance. {@code bankIndex} is the slot its withdraws must act on,
 * and {@code templateChild} the child whose look a twin copies; both are -1 when they do not apply,
 * as is {@code quantity} when the game's own count should stand. An empty {@code name} leaves the
 * name the child already has, which is how an item the player holds keeps its own menu text.
 */
@Value
@Accessors(fluent = true)
public class SlotFace {

    public enum Kind {
        /** The child the game itself drew this item at, moved into place. */
        OWNED,
        /** A further copy of an item the player holds. */
        TWIN,
        /** An item the bank cannot supply, drawn ghosted and without a menu. */
        PLACEHOLDER
    }

    public static final int NONE = -1;

    Kind kind;
    BankSlotPlan plan;
    String name;
    int bankIndex;
    int templateChild;
    int x;
    int y;
    int opacity;
    int quantity;
    boolean hideQuantity;

    public SlotFace(Kind kind, BankSlotPlan plan, String name, int bankIndex, int templateChild,
                    int x, int y, int opacity, int quantity, boolean hideQuantity) {
        this.kind = Objects.requireNonNull(kind, "kind");
        this.plan = Objects.requireNonNull(plan, "plan");
        this.name = Objects.requireNonNull(name, "name");
        if (kind == Kind.TWIN && templateChild == NONE) {
            throw new IllegalArgumentException("A twin needs a template to copy");
        }
        this.bankIndex = bankIndex;
        this.templateChild = templateChild;
        this.x = x;
        this.y = y;
        this.opacity = opacity;
        this.quantity = quantity;
        this.hideQuantity = hideQuantity;
    }

    public boolean isPlaceholder() {
        return kind == Kind.PLACEHOLDER;
    }

    public boolean hasQuantity() {
        return quantity != NONE;
    }

    public boolean hasName() {
        return !name.isEmpty();
    }

    /** The same face dimmed, for an item a live search does not match. */
    public SlotFace dimmed(int dimOpacity) {
        return new SlotFace(kind, plan, name, bankIndex, templateChild, x, y, dimOpacity, quantity, hideQuantity);
    }
}
