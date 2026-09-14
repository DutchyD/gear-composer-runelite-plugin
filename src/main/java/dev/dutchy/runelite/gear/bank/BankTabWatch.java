package dev.dutchy.runelite.gear.bank;

import dev.dutchy.runelite.gear.activation.SetupActivator;
import net.runelite.api.Client;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.gameval.VarbitID;
import net.runelite.client.eventbus.Subscribe;

import javax.inject.Inject;
import java.util.Objects;

/** Clicking a bank tab is the way back to a normal bank: it turns the shown setup off. */
public final class BankTabWatch {

    private static final int UNKNOWN = -1;

    private final Client client;
    private final SetupActivator activator;
    private int lastTab = UNKNOWN;

    @Inject
    public BankTabWatch(Client client, SetupActivator activator) {
        this.client = Objects.requireNonNull(client, "client");
        this.activator = Objects.requireNonNull(activator, "activator");
    }

    /** Reads the current tab; client thread only. */
    public void refresh() {
        lastTab = client.getVarbitValue(VarbitID.BANK_CURRENTTAB);
    }

    @Subscribe
    public void onVarbitChanged(VarbitChanged event) {
        if (event.getVarbitId() != VarbitID.BANK_CURRENTTAB) {
            return;
        }
        int tab = event.getValue();
        boolean changed = lastTab != UNKNOWN && tab != lastTab;
        lastTab = tab;
        if (changed && activator.current().isPresent()) {
            activator.clear();
        }
    }
}
