package dev.dutchy.runelite.gear.share;

import java.util.Optional;

/** The system clipboard, as much of it as sharing needs. */
public interface Clipboard {

    void copy(String text);

    /** The clipboard's text, empty when it holds none or cannot be read. */
    Optional<String> paste();
}
