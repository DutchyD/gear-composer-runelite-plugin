package dev.dutchy.runelite.gear.ui;

import dev.dutchy.runelite.gear.Dispatch;

import javax.swing.*;

/** The book's write thread in production. */
public final class SwingDispatch implements Dispatch {

    @Override
    public boolean isCurrent() {
        return SwingUtilities.isEventDispatchThread();
    }
}
