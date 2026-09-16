package dev.dutchy.runelite.gear.bank;

import dev.dutchy.runelite.gear.config.GearComposerConfig;
import net.runelite.api.Client;
import net.runelite.api.MenuEntry;
import net.runelite.api.events.PostMenuSort;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.client.eventbus.Subscribe;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Makes the withdraw option that matches the setup's amount the left-click while a setup is shown.
 * Only the order of the game's own entries changes, as RuneLite's menu entry swapper does.
 */
public final class WithdrawMenuSwapper {

    private final Client client;
    private final DrawnBankSource bank;
    private final GearComposerConfig config;

    @Inject
    public WithdrawMenuSwapper(Client client, DrawnBankSource bank, GearComposerConfig config) {
        this.client = Objects.requireNonNull(client, "client");
        this.bank = Objects.requireNonNull(bank, "bank");
        this.config = Objects.requireNonNull(config, "config");
    }

    @Subscribe
    public void onPostMenuSort(PostMenuSort event) {
        DrawnBank shown = bank.drawnBank();
        if (!config.withdrawSetupAmount() || !shown.active()) {
            return;
        }
        MenuEntry[] entries = client.getMenu().getMenuEntries();
        if (entries.length == 0) {
            return;
        }
        MenuEntry top = entries[entries.length - 1];
        if (top.getParam1() != InterfaceID.Bankmain.ITEMS) {
            return;
        }
        Optional<DrawnSlot> slot = shown.at(top.getParam0());
        if (slot.isEmpty() || !slot.get().plan().item().hasQuantity()) {
            return;
        }
        List<String> options = new ArrayList<>();
        for (MenuEntry entry : entries) {
            options.add(entry.getOption());
        }
        Optional<String> preferred = WithdrawOption.preferred(slot.get().required(), options);
        if (preferred.isEmpty() || preferred.get().equals(top.getOption())) {
            return;
        }
        client.getMenu().setMenuEntries(reordered(entries, preferred.get()));
    }

    /** The preferred entry moved to the end, which the client treats as the left-click option. */
    static MenuEntry[] reordered(MenuEntry[] entries, String preferredOption) {
        List<MenuEntry> others = new ArrayList<>();
        MenuEntry chosen = null;
        for (MenuEntry entry : entries) {
            if (chosen == null && preferredOption.equals(entry.getOption())) {
                chosen = entry;
            } else {
                others.add(entry);
            }
        }
        if (chosen == null) {
            return entries;
        }
        others.add(chosen);
        return others.toArray(new MenuEntry[0]);
    }
}
