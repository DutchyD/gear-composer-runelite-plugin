package dev.dutchy.runelite.gear.ui;

import dev.dutchy.runelite.gear.content.DividerChange;
import dev.dutchy.runelite.gear.content.SlotRef;
import dev.dutchy.runelite.gear.content.SyncScope;
import dev.dutchy.runelite.libs.ui.item.ResolvedItem;

import java.util.List;

/** What the contents page can ask of its host. */
interface ContentActions extends VariantActions {

    void editSlot(SlotRef ref);

    void clearSlot(SlotRef ref);

    void clearSlots(List<SlotRef> refs);

    void moveItem(SlotRef from, SlotRef to, boolean copy);

    void dropItem(ResolvedItem item, SlotRef to);

    void fillRemaining(SlotRef from);

    void fillRow(SlotRef from);

    /** Opens the divider page for the divider over the slot's column, or to start one there. */
    void editDivider(SlotRef slot);

    void removeDivider(SlotRef slot);

    void changeDivider(SlotRef slot, DividerChange change);

    void copySlots(List<SlotRef> refs);

    void pasteAt(SlotRef anchor);

    boolean canPaste();

    void sync(SyncScope scope);

    void showHistory();

    void back();

    void compare();

    void share();
}
