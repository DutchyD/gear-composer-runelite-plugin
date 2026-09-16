package dev.dutchy.runelite.gear.bank;

/**
 * Applies a planned layout to the in-game bank. Items the player does not hold are expected to be
 * drawn as placeholders.
 */
public interface BankLayoutApplier {

    void apply(BankLayout layout);

    /** Takes the layout down and redraws the bank as the game would. */
    void clear();

    /** Forgets the layout without redrawing, for when the bank is not on screen. */
    default void discard() {
        clear();
    }

    default boolean isBankOpen() {
        return false;
    }
}
