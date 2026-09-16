package dev.dutchy.runelite.gear;

import lombok.Value;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.Objects;
import java.util.Optional;

/** A key plus modifiers, stored independently of any client library; {@link #NONE} means no key is bound. */
@Value
public class Hotkey {
    int keyCode;
    int modifiers;

    public static final Hotkey NONE = new Hotkey(KeyEvent.VK_UNDEFINED, 0);

    private static final int MODIFIER_MASK = InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK
            | InputEvent.ALT_DOWN_MASK | InputEvent.META_DOWN_MASK;

    public Hotkey(int keyCode, int modifiers) {
        modifiers = keyCode == KeyEvent.VK_UNDEFINED ? 0 : modifiers & MODIFIER_MASK;
        this.keyCode = keyCode;
        this.modifiers = modifiers;
    }

    /** @throws IllegalArgumentException when the event carries no key */
    public static Hotkey of(KeyEvent event) {
        Objects.requireNonNull(event, "event");
        if (event.getKeyCode() == KeyEvent.VK_UNDEFINED) {
            throw new IllegalArgumentException("A hotkey needs a key");
        }
        return new Hotkey(event.getKeyCode(), event.getModifiersEx());
    }

    public boolean isSet() {
        return keyCode != KeyEvent.VK_UNDEFINED;
    }

    public boolean matches(KeyEvent event) {
        return isSet() && event.getKeyCode() == keyCode && (event.getModifiersEx() & MODIFIER_MASK) == modifiers;
    }

    public String describe() {
        if (!isSet()) {
            return "";
        }
        String key = KeyEvent.getKeyText(keyCode);
        return modifiers == 0 ? key : InputEvent.getModifiersExText(modifiers) + "+" + key;
    }

    /** The "keyCode,modifiers" form used in storage. */
    public String serialize() {
        return keyCode + "," + modifiers;
    }

    public static Optional<Hotkey> parse(String text) {
        if (text == null) {
            return Optional.empty();
        }
        String[] parts = text.split(",");
        if (parts.length != 2) {
            return Optional.empty();
        }
        try {
            Hotkey hotkey = new Hotkey(Integer.parseInt(parts[0].strip()), Integer.parseInt(parts[1].strip()));
            return hotkey.isSet() ? Optional.of(hotkey) : Optional.empty();
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }
}
