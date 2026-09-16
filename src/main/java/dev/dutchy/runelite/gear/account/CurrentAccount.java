package dev.dutchy.runelite.gear.account;

import java.util.Optional;

/** The RuneScape character that is logged in right now, if any. Listeners run on the EDT. */
public interface CurrentAccount {

    /** A stable key for the character, empty while logged out. */
    Optional<String> key();

    /** Something to show the player, such as the character name. */
    Optional<String> displayName();

    void addListener(Runnable listener);
}
