package dev.dutchy.runelite.gear.bank;

import net.runelite.api.Client;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.gameval.VarbitID;
import net.runelite.client.eventbus.Subscribe;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Objects;

/**
 * How many items each numbered bank tab holds, read when the game changes one rather than when
 * something asks. The slot overlay needs these for every item it draws, so reading nine varbits per
 * item per frame is work worth doing once.
 */
@Singleton
public final class BankTabSizes {

    private static final int[] VARBITS = {
            VarbitID.BANK_TAB_1, VarbitID.BANK_TAB_2, VarbitID.BANK_TAB_3, VarbitID.BANK_TAB_4, VarbitID.BANK_TAB_5,
            VarbitID.BANK_TAB_6, VarbitID.BANK_TAB_7, VarbitID.BANK_TAB_8, VarbitID.BANK_TAB_9};
    private static final int[] UNREAD = new int[VARBITS.length];

    private final Client client;

    private volatile int[] counts = UNREAD;

    @Inject
    public BankTabSizes(Client client) {
        this.client = Objects.requireNonNull(client, "client");
    }

    /** Re-reads every tab size; client thread only. */
    public void refresh() {
        int[] read = new int[VARBITS.length];
        for (int index = 0; index < VARBITS.length; index++) {
            read[index] = client.getVarbitValue(VARBITS[index]);
        }
        counts = read;
    }

    /** The sizes as last read, tab 1 first. Callers must only read it. */
    public int[] counts() {
        return counts;
    }

    @Subscribe
    public void onVarbitChanged(VarbitChanged event) {
        for (int varbit : VARBITS) {
            if (event.getVarbitId() == varbit) {
                refresh();
                return;
            }
        }
    }
}
