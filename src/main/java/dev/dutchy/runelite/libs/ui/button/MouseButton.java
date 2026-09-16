package dev.dutchy.runelite.libs.ui.button;

import javax.swing.*;
import java.awt.event.MouseEvent;

public enum MouseButton {

    LEFT,
    MIDDLE,
    RIGHT,
    OTHER;

    public static MouseButton from(MouseEvent event) {
        if (SwingUtilities.isLeftMouseButton(event)) {
            return LEFT;
        }
        if (SwingUtilities.isRightMouseButton(event)) {
            return RIGHT;
        }
        if (SwingUtilities.isMiddleMouseButton(event)) {
            return MIDDLE;
        }
        return OTHER;
    }
}
