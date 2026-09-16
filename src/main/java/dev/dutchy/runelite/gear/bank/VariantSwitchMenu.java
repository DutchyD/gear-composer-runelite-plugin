package dev.dutchy.runelite.gear.bank;

import dev.dutchy.runelite.gear.content.ItemQuantityFormat;
import dev.dutchy.runelite.libs.ui.item.ItemId;
import lombok.Value;
import net.runelite.api.Client;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.events.MenuOpened;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.client.eventbus.Subscribe;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

/**
 * Adds a plugin-owned entry per other dose or charge the bank holds to a laid-out slot's menu, so the
 * player can draw that stack in the slot and then withdraw from it as usual. The entries only change
 * what the plugin draws; the game's own entries are left alone.
 */
public final class VariantSwitchMenu {

    static final String OPTION = "Show here";
    private static final int ABOVE_CANCEL = 1;

    /** One entry: the words to show and the stack it would draw. */
    @Value
    public static class Entry {
        String target;
        ItemId id;

        public Entry(String target, ItemId id) {
            this.target = Objects.requireNonNull(target, "target");
            this.id = Objects.requireNonNull(id, "id");
        }
    }

    private final Client client;
    private final DrawnBankSource bank;
    private final SlotChooser chooser;
    private final BankItemNames names;

    @Inject
    public VariantSwitchMenu(Client client, DrawnBankSource bank, SlotChooser chooser, BankItemNames names) {
        this.client = Objects.requireNonNull(client, "client");
        this.bank = Objects.requireNonNull(bank, "bank");
        this.chooser = Objects.requireNonNull(chooser, "chooser");
        this.names = Objects.requireNonNull(names, "names");
    }

    @Subscribe
    public void onMenuOpened(MenuOpened event) {
        DrawnBank shown = bank.drawnBank();
        MenuEntry first = event.getFirstEntry();
        if (!shown.active() || first == null || first.getParam1() != InterfaceID.Bankmain.ITEMS) {
            return;
        }
        Optional<DrawnSlot> slot = shown.at(first.getParam0());
        if (slot.isEmpty()) {
            return;
        }
        int slotIndex = slot.get().plan().slotIndex();
        for (Entry entry : entriesFor(slot.get(), names::of)) {
            client.getMenu().createMenuEntry(ABOVE_CANCEL)
                    .setOption(OPTION)
                    .setTarget(entry.target())
                    .setType(MenuAction.RUNELITE)
                    .setParam0(first.getParam0())
                    .setParam1(first.getParam1())
                    .onClick(clicked -> chooser.show(slotIndex, entry.id()));
        }
    }

    /** One entry per stack the slot could draw instead, in the slot's order of preference. */
    public static List<Entry> entriesFor(DrawnSlot slot, Function<ItemId, String> names) {
        Objects.requireNonNull(slot, "slot");
        Objects.requireNonNull(names, "names");
        List<Entry> entries = new ArrayList<>();
        for (SlotSupply.Stack stack : slot.plan().supply().others()) {
            entries.add(new Entry("<col=ff9040>" + names.apply(stack.id()) + "</col> x" + ItemQuantityFormat.text(stack.count()), stack.id()));
        }
        return List.copyOf(entries);
    }

}
