package dev.dutchy.runelite.gear;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.Optional;
import javax.swing.JLabel;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class HotkeyTest {

    @Test
    void roundTripsThroughItsStoredForm() {
        Hotkey hotkey = new Hotkey(KeyEvent.VK_F5, InputEvent.CTRL_DOWN_MASK);
        assertEquals(Optional.of(hotkey), Hotkey.parse(hotkey.serialize()));
        assertEquals(Optional.empty(), Hotkey.parse("garbage"));
        assertEquals(Optional.empty(), Hotkey.parse(null));
    }

    @Test
    void onlyRealModifiersCount() {
        Hotkey hotkey = new Hotkey(KeyEvent.VK_A, InputEvent.CTRL_DOWN_MASK | InputEvent.BUTTON1_DOWN_MASK);
        assertEquals(InputEvent.CTRL_DOWN_MASK, hotkey.modifiers());
        assertEquals("Ctrl+A", hotkey.describe());
    }

    @Test
    void anUndefinedKeyMeansNoHotkey() {
        assertEquals(Hotkey.NONE, new Hotkey(KeyEvent.VK_UNDEFINED, InputEvent.CTRL_DOWN_MASK));
        assertEquals("", Hotkey.NONE.describe());
        assertEquals(Optional.empty(), Hotkey.parse(Hotkey.NONE.serialize()));
        KeyEvent noKey = new KeyEvent(new JLabel(), KeyEvent.KEY_PRESSED, 0, 0, KeyEvent.VK_UNDEFINED, KeyEvent.CHAR_UNDEFINED);
        assertThrows(IllegalArgumentException.class, () -> Hotkey.of(noKey));
        assertFalse(Hotkey.NONE.matches(noKey));
    }
}
