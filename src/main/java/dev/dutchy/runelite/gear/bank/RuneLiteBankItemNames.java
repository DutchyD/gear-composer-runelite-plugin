package dev.dutchy.runelite.gear.bank;

import dev.dutchy.runelite.libs.ui.item.ItemId;
import net.runelite.api.Client;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Objects;

/** Must be read on the client thread. */
@Singleton
public final class RuneLiteBankItemNames implements BankItemNames {

    private final Client client;

    @Inject
    public RuneLiteBankItemNames(Client client) {
        this.client = Objects.requireNonNull(client, "client");
    }

    @Override
    public String of(ItemId item) {
        String name = client.getItemDefinition(item.value()).getName();
        return name == null || name.isBlank() ? BankItemNames.unknown(item) : name;
    }
}
