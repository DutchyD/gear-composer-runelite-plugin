package dev.dutchy.runelite.gear.ui;

import dev.dutchy.runelite.gear.content.BankContent;
import dev.dutchy.runelite.gear.content.GridKind;
import dev.dutchy.runelite.gear.content.SyncScope;
import dev.dutchy.runelite.gear.guide.HelpTopic;
import dev.dutchy.runelite.libs.ui.icon.ItemIconFactory;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

/** The two sides of a bank layout, each with its own sync button. */
final class BankBody extends ContentBody {

    private final List<SlotView> slots = new ArrayList<>();

    BankBody(BankContent bank, ItemIconFactory icons, SlotView.Listener listener, ContentActions actions) {
        ItemGridView left = new ItemGridView(GridKind.LEFT, bank.left(), icons, listener);
        ItemGridView right = new ItemGridView(GridKind.RIGHT, bank.right(), icons, listener);
        slots.addAll(left.slots());
        slots.addAll(right.slots());
        add(heading("Left side", syncButton(SetupContentPanel.SYNC_SIDE,
                "Replace the left side with what you are carrying", () -> actions.sync(SyncScope.LEFT_SIDE))));
        add(Help.anchor(centered(left), HelpTopic.BANK_SIDES));
        add(Box.createVerticalStrut(Ui.GAP));
        add(heading("Right side", syncButton(SetupContentPanel.SYNC_SIDE,
                "Replace the right side with what you are carrying", () -> actions.sync(SyncScope.RIGHT_SIDE))));
        add(centered(right));
    }

    @Override
    List<SlotView> slots() {
        return List.copyOf(slots);
    }

    @Override
    void show(ContentViewState view) {
    }

    @Override
    ContentViewState remember(ContentViewState view) {
        return view;
    }
}
