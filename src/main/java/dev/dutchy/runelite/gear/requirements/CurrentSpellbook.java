package dev.dutchy.runelite.gear.requirements;

import dev.dutchy.runelite.gear.Spellbook;

import java.util.Optional;

/** The spellbook the player is on right now, empty while logged out. Listeners run on the EDT. */
public interface CurrentSpellbook {

    Optional<Spellbook> current();

    void addListener(Runnable listener);
}
