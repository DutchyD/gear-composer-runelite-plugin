package dev.dutchy.runelite.libs.ui.selector;

import dev.dutchy.runelite.libs.ui.item.ResolvedItem;

@FunctionalInterface
public interface ItemSelectionListener {

    void onItemSelected(ResolvedItem item);
}
