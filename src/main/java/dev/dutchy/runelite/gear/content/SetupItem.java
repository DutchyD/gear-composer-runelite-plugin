package dev.dutchy.runelite.gear.content;

import dev.dutchy.runelite.libs.ui.item.ItemId;
import lombok.Value;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.OptionalInt;

/**
 * An item in a setup. An amount of {@link #BANK_AMOUNT} means "however many the bank holds"; read it
 * through {@link #quantity()}. Alternatives are acceptable stand-ins, in order of preference, that
 * share the item's match rule.
 */
@Value
public class SetupItem {
    ItemId id;
    int amount;
    ItemMatch match;
    List<ItemId> alternatives;
    boolean noted;

    public static final int MIN_QUANTITY = 1;
    /** The amount that means "whatever the bank holds". */
    public static final int BANK_AMOUNT = 0;
    public static final int MAX_ALTERNATIVES = 8;

    public SetupItem(ItemId id, int amount, ItemMatch match, List<ItemId> alternatives) {
        this(id, amount, match, alternatives, false);
    }

    /** @param noted whether the slot wants the item withdrawn as a note */
    public SetupItem(ItemId id, int amount, ItemMatch match, List<ItemId> alternatives, boolean noted) {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(match, "match");
        Objects.requireNonNull(alternatives, "alternatives");
        if (amount != BANK_AMOUNT && amount < MIN_QUANTITY) {
            throw new IllegalArgumentException("Quantity must be at least " + MIN_QUANTITY + ", got " + amount);
        }
        alternatives = distinctAlternatives(id, alternatives);
        this.id = id;
        this.amount = amount;
        this.match = match;
        this.alternatives = alternatives;
        this.noted = noted;
    }

    /** An item taking whatever the bank holds. */
    public SetupItem(ItemId id) {
        this(id, BANK_AMOUNT, ItemMatch.DEFAULT, List.of());
    }

    public static SetupItem of(int itemId) {
        return new SetupItem(ItemId.of(itemId));
    }

    /** @throws IllegalArgumentException for an amount below one; use {@link #of(int)} for the bank amount */
    public static SetupItem of(int itemId, int quantity) {
        return new SetupItem(ItemId.of(itemId)).withQuantity(quantity);
    }

    /** The set amount, empty when the item takes whatever the bank holds. */
    public OptionalInt quantity() {
        return amount == BANK_AMOUNT ? OptionalInt.empty() : OptionalInt.of(amount);
    }

    public SetupItem withQuantity(int newQuantity) {
        if (newQuantity < MIN_QUANTITY) {
            throw new IllegalArgumentException("Quantity must be at least " + MIN_QUANTITY + ", got " + newQuantity);
        }
        return new SetupItem(id, newQuantity, match, alternatives, noted);
    }

    public SetupItem withoutQuantity() {
        return new SetupItem(id, BANK_AMOUNT, match, alternatives, noted);
    }

    public SetupItem withMatch(ItemMatch newMatch) {
        return new SetupItem(id, amount, newMatch, alternatives, noted);
    }

    /** Appends a stand-in unless it is already listed or is the item itself. */
    public SetupItem withAlternative(ItemId alternative) {
        Objects.requireNonNull(alternative, "alternative");
        if (alternative.equals(id) || alternatives.contains(alternative)) {
            return this;
        }
        if (alternatives.size() >= MAX_ALTERNATIVES) {
            throw new IllegalArgumentException("At most " + MAX_ALTERNATIVES + " alternatives are allowed");
        }
        List<ItemId> extended = new ArrayList<>(alternatives);
        extended.add(alternative);
        return new SetupItem(id, amount, match, extended, noted);
    }

    public SetupItem withoutAlternative(ItemId alternative) {
        Objects.requireNonNull(alternative, "alternative");
        if (!alternatives.contains(alternative)) {
            return this;
        }
        List<ItemId> reduced = new ArrayList<>(alternatives);
        reduced.remove(alternative);
        return new SetupItem(id, amount, match, reduced, noted);
    }

    /** The item and then its alternatives, in order of preference. */
    public List<ItemId> candidates() {
        List<ItemId> candidates = new ArrayList<>(alternatives.size() + 1);
        candidates.add(id);
        candidates.addAll(alternatives);
        return List.copyOf(candidates);
    }

    /** How many to show for a bank holding {@code held}: never more than the player actually has. */
    public int shownQuantity(int held) {
        return amount == BANK_AMOUNT ? held : Math.min(amount, held);
    }

    public boolean hasQuantity() {
        return amount != BANK_AMOUNT;
    }

    public SetupItem withNoted(boolean isNoted) {
        return new SetupItem(id, amount, match, alternatives, isNoted);
    }

    public boolean hasAlternatives() {
        return !alternatives.isEmpty();
    }

    /** Whether a number should be drawn over the sprite. */
    public boolean showsQuantity() {
        return amount > MIN_QUANTITY;
    }

    private static List<ItemId> distinctAlternatives(ItemId primary, List<ItemId> alternatives) {
        List<ItemId> distinct = new ArrayList<>();
        for (ItemId alternative : alternatives) {
            Objects.requireNonNull(alternative, "alternative");
            if (!alternative.equals(primary) && !distinct.contains(alternative)) {
                distinct.add(alternative);
            }
        }
        if (distinct.size() > MAX_ALTERNATIVES) {
            throw new IllegalArgumentException("At most " + MAX_ALTERNATIVES + " alternatives are allowed, got " + distinct.size());
        }
        return List.copyOf(distinct);
    }
}
