package dev.dutchy.runelite.gear.bank;

import dev.dutchy.runelite.gear.activation.SetupActivator;
import net.runelite.api.ScriptID;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.events.ScriptPreFired;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.client.eventbus.Subscribe;

import javax.inject.Inject;
import java.util.Objects;

/** Opening the bank search is the way back to a normal bank: it turns the shown setup off before the search starts. */
public final class BankSearchWatch {

    private final SetupActivator activator;

    @Inject
    public BankSearchWatch(SetupActivator activator) {
        this.activator = Objects.requireNonNull(activator, "activator");
    }

    @Subscribe
    public void onScriptPreFired(ScriptPreFired event) {
        if (event.getScriptId() == ScriptID.BANKMAIN_SEARCH_TOGGLE) {
            clearLayout();
        }
    }

    @Subscribe
    public void onMenuOptionClicked(MenuOptionClicked event) {
        if (event.getParam1() == InterfaceID.Bankmain.SEARCH) {
            clearLayout();
        }
    }

    private void clearLayout() {
        if (activator.current().isPresent()) {
            activator.clear();
        }
    }
}
