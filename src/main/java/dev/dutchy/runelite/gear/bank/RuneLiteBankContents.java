package dev.dutchy.runelite.gear.bank;

import dev.dutchy.runelite.libs.ui.item.ItemId;
import net.runelite.api.Client;
import net.runelite.api.ItemContainer;
import net.runelite.api.gameval.InventoryID;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Objects;

/** Must be read on the client thread. */
@Singleton
public final class RuneLiteBankContents implements BankContents {

    private final Client client;

    @Inject
    public RuneLiteBankContents(Client client) {
        this.client = Objects.requireNonNull(client, "client");
    }

    @Override
    public boolean contains(ItemId item) {
        return count(item) > 0;
    }

    @Override
    public int count(ItemId item) {
        Objects.requireNonNull(item, "item");
        ItemContainer bank = client.getItemContainer(InventoryID.BANK);
        return bank == null ? 0 : bank.count(item.value());
    }
}
