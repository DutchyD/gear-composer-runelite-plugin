package dev.dutchy.runelite.gear.player;

import dev.dutchy.runelite.gear.content.Loadout;

import java.util.function.Consumer;

/** Reads what the player is wearing and carrying. Both callbacks run on the EDT; only one of them runs. */
@FunctionalInterface
public interface PlayerItems {

    void capture(Consumer<Loadout> onCaptured, Runnable onUnavailable);
}
