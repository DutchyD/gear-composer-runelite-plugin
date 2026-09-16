package dev.dutchy.runelite.gear.ui;

import java.util.Optional;

/**
 * How a sidebar module asks the player something and tells them what happened. Swing sits behind it,
 * so a module that only talks through here can be tested without a screen.
 */
public interface Prompts {

    /** Yes or no, false when there is nobody to ask. */
    boolean confirm(String question, String title);

    /** The text the player typed, empty when they backed out. */
    Optional<String> askForName(String question, String initial);

    /** Reports something that did not change the book. */
    void say(String message);

    /** Reports a change to the book and offers to take it back. */
    void announce(String message);
}
