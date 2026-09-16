package dev.dutchy.runelite.libs.ui.swing;

import javax.swing.*;
import java.util.Objects;

/** Where Swing work belongs: the event dispatch thread. */
public final class EdtDispatch {

    private EdtDispatch() {
    }

    /** Runs the task on the EDT, inline when already there. */
    public static void onEdt(Runnable task) {
        Objects.requireNonNull(task, "task");
        if (SwingUtilities.isEventDispatchThread()) {
            task.run();
        } else {
            SwingUtilities.invokeLater(task);
        }
    }

    public static void requireEdt() {
        if (!SwingUtilities.isEventDispatchThread()) {
            throw new IllegalStateException("Must be called on the Swing event dispatch thread");
        }
    }
}
