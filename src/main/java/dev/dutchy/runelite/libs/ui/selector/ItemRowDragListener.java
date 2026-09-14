package dev.dutchy.runelite.libs.ui.selector;

import dev.dutchy.runelite.libs.ui.item.ResolvedItem;

import java.awt.event.MouseEvent;

/** Receives the raw mouse gesture on a result row so a host can turn it into a drag. */
public interface ItemRowDragListener {

    void pressed(ResolvedItem item, MouseEvent event);

    void dragged(ResolvedItem item, MouseEvent event);

    void released(ResolvedItem item, MouseEvent event);
}
