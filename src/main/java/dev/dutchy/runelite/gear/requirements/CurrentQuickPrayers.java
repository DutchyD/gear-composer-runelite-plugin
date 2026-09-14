package dev.dutchy.runelite.gear.requirements;

import dev.dutchy.runelite.gear.Prayer;

import java.util.Optional;
import java.util.Set;

/** The quick prayers the player has selected right now, empty while logged out. Listeners run on the EDT. */
public interface CurrentQuickPrayers {

    Optional<Set<Prayer>> selected();

    void addListener(Runnable listener);
}
