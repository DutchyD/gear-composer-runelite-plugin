package dev.dutchy.runelite.gear.persistence;

import java.util.Optional;

/** Where the encoded book lives. Implementations must be safe to call from any thread. */
public interface BookStore {

    Optional<String> read();

    void write(String encoded);

    /** Sets aside an unreadable document so a later save cannot destroy it. */
    void quarantine(String encoded);
}
