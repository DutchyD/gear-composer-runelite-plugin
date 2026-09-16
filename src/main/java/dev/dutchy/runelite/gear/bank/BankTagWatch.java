package dev.dutchy.runelite.gear.bank;

import dev.dutchy.runelite.gear.activation.SetupActivator;
import net.runelite.api.ScriptID;
import net.runelite.api.events.ScriptPreFired;
import net.runelite.client.eventbus.Subscribe;

import javax.inject.Inject;
import java.util.Objects;

/** Opening a bank tag is the way back to a normal bank: it turns the shown setup off. */
public final class BankTagWatch {

    private final SetupActivator activator;
    private final BankTagState tags;

    @Inject
    public BankTagWatch(SetupActivator activator, BankTagState tags) {
        this.activator = Objects.requireNonNull(activator, "activator");
        this.tags = Objects.requireNonNull(tags, "tags");
    }

    /** Runs at default priority, before the screen draws, so a build under a tag is left to the game. */
    @Subscribe
    public void onScriptPreFired(ScriptPreFired event) {
        if (event.getScriptId() == ScriptID.BANKMAIN_FINISHBUILDING && tags.isTagOpen()
                && activator.current().isPresent()) {
            activator.clear();
        }
    }
}
