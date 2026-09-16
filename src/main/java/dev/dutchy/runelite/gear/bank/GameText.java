package dev.dutchy.runelite.gear.bank;

import java.util.Objects;

/**
 * Prepares text for the game's own renderer, which reads an angle bracket as the start of a tag and
 * swallows anything that follows. Every name a player chose passes through here before it reaches a
 * widget, so a setup called with a bracket renders as typed instead of vanishing.
 */
final class GameText {

    private GameText() {
    }

    static String escape(String text) {
        Objects.requireNonNull(text, "text");
        StringBuilder escaped = new StringBuilder(text.length());
        for (int i = 0; i < text.length(); i++) {
            char character = text.charAt(i);
            if (character == '<') {
                escaped.append("<lt>");
            } else if (character == '>') {
                escaped.append("<gt>");
            } else {
                escaped.append(character);
            }
        }
        return escaped.toString();
    }
}
