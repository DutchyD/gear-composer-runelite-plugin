package dev.dutchy.runelite.gear.activation;

import dev.dutchy.runelite.gear.GearSetup;
import dev.dutchy.runelite.gear.GearSetupBook;
import dev.dutchy.runelite.gear.Hotkey;
import dev.dutchy.runelite.gear.bank.ActiveSetup;
import dev.dutchy.runelite.gear.bank.BankLayout;
import dev.dutchy.runelite.gear.bank.BankLayoutApplier;
import dev.dutchy.runelite.gear.bank.BankLayoutPlanner;
import dev.dutchy.runelite.gear.config.GearComposerConfig;
import dev.dutchy.runelite.gear.content.GearContent;
import dev.dutchy.runelite.gear.content.SetupVariant;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.Optional;
import javax.swing.JLabel;
import net.runelite.client.config.Keybind;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SetupHotkeysTest {

    private final GearSetupBook book = new GearSetupBook();
    private final ActiveSetup active = new ActiveSetup();
    private final SetupActivator activator = new SetupActivator(book, active, new BankLayoutPlanner(), new BankLayoutApplier() {
        @Override
        public void apply(BankLayout layout) {
        }

        @Override
        public void clear() {
        }
    });
    private final GearComposerConfig config = new GearComposerConfig() {
        @Override
        public Keybind nextSetupHotkey() {
            return new Keybind(KeyEvent.VK_F7, 0);
        }

        @Override
        public Keybind nextVariantHotkey() {
            return new Keybind(KeyEvent.VK_F8, 0);
        }

        @Override
        public Keybind previousVariantHotkey() {
            return new Keybind(KeyEvent.VK_F8, InputEvent.SHIFT_DOWN_MASK);
        }
    };
    private final SetupHotkeys hotkeys = new SetupHotkeys(book, activator, config);

    @SuppressWarnings("MagicConstant")
    private static KeyEvent press(int keyCode, int modifiers) {
        return new KeyEvent(new JLabel(), KeyEvent.KEY_PRESSED, 0, modifiers, keyCode, KeyEvent.CHAR_UNDEFINED);
    }

    @Test
    void aSetupHotkeyTogglesThatSetup() {
        GearSetup vorkath = book.addSetup(book.sections().get(0).id(), "Vorkath");
        book.changeMeta(vorkath.id(), meta -> meta.withHotkey(new Hotkey(KeyEvent.VK_F5, InputEvent.CTRL_DOWN_MASK)));

        hotkeys.actionFor(press(KeyEvent.VK_F5, InputEvent.CTRL_DOWN_MASK)).orElseThrow().run();
        assertTrue(active.isActive(vorkath.id()));
        assertTrue(hotkeys.actionFor(press(KeyEvent.VK_F5, 0)).isEmpty(), "modifiers must match");
    }

    @Test
    void theVariantKeysStepThroughTheActiveSetupsVariants() {
        GearSetup a = book.addSetup(book.sections().get(0).id(), "A");
        book.change(a.id(), current -> current.withAddedVariant(new SetupVariant("Mage",
                GearContent.empty())).withSelectedVariant(0));
        activator.toggle(a.id());

        hotkeys.actionFor(press(KeyEvent.VK_F8, 0)).orElseThrow().run();
        assertEquals("Mage", book.setup(a.id()).orElseThrow().variant().name());
        hotkeys.actionFor(press(KeyEvent.VK_F8, InputEvent.SHIFT_DOWN_MASK)).orElseThrow().run();
        assertEquals("Default", book.setup(a.id()).orElseThrow().variant().name());
    }

    @Test
    void theConfiguredCycleKeyStepsThroughSetups() {
        GearSetup a = book.addSetup(book.sections().get(0).id(), "A");
        hotkeys.actionFor(press(KeyEvent.VK_F7, 0)).orElseThrow().run();
        assertTrue(active.isActive(a.id()));
        assertEquals(Optional.empty(), hotkeys.actionFor(press(KeyEvent.VK_F9, 0)));
    }
}
