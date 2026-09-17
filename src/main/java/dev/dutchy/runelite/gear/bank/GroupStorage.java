package dev.dutchy.runelite.gear.bank;

import net.runelite.api.Client;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.widgets.Widget;

/** Group storage opens over the bank while the bank stays loaded beneath it, so overlays drawn on the bank must step aside. */
final class GroupStorage {

    private GroupStorage() {
    }

    static boolean isOpen(Client client) {
        Widget storage = client.getWidget(InterfaceID.SharedBank.UNIVERSE);
        return storage != null && !storage.isHidden();
    }
}
