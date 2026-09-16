package dev.dutchy.runelite.gear.activation;

import dev.dutchy.runelite.gear.GearSetup;
import dev.dutchy.runelite.gear.GearSetupBook;
import dev.dutchy.runelite.gear.Hotkey;
import dev.dutchy.runelite.gear.config.GearComposerConfig;
import dev.dutchy.runelite.libs.ui.swing.EdtDispatch;
import net.runelite.client.config.Keybind;
import net.runelite.client.input.KeyListener;

import javax.inject.Inject;
import java.awt.event.KeyEvent;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/** Turns key presses on the game canvas into setup switches. Registered with RuneLite's key manager. */
public final class SetupHotkeys implements KeyListener {

    private final GearSetupBook book;
    private final SetupActivator activator;
    private final GearComposerConfig config;

    @Inject
    public SetupHotkeys(GearSetupBook book, SetupActivator activator, GearComposerConfig config) {
        this.book = Objects.requireNonNull(book, "book");
        this.activator = Objects.requireNonNull(activator, "activator");
        this.config = Objects.requireNonNull(config, "config");
    }

    @Override
    public void keyPressed(KeyEvent event) {
        Optional<Runnable> action = actionFor(event);
        if (action.isPresent()) {
            event.consume();
            EdtDispatch.onEdt(action.get());
        }
    }

    /** What the key would do, if anything; exposed so tests need no key manager. */
    public Optional<Runnable> actionFor(KeyEvent event) {
        Objects.requireNonNull(event, "event");
        if (matches(config.nextSetupHotkey(), event)) {
            return Optional.of(activator::next);
        }
        if (matches(config.previousSetupHotkey(), event)) {
            return Optional.of(activator::previous);
        }
        if (matches(config.clearLayoutHotkey(), event)) {
            return Optional.of(activator::clear);
        }
        if (matches(config.nextVariantHotkey(), event)) {
            return Optional.of(activator::nextVariant);
        }
        if (matches(config.previousVariantHotkey(), event)) {
            return Optional.of(activator::previousVariant);
        }
        for (GearSetup setup : book.sections().stream().flatMap(section -> section.setups().stream()).collect(Collectors.toList())) {
            if (setup.meta().hotkey().matches(event)) {
                return Optional.of(() -> activator.toggle(setup.id()));
            }
        }
        return Optional.empty();
    }

    /** Compares by key code rather than RuneLite's extended code, which synthetic events lack. */
    private static boolean matches(Keybind keybind, KeyEvent event) {
        if (keybind == null || keybind == Keybind.NOT_SET || keybind.getKeyCode() == KeyEvent.VK_UNDEFINED) {
            return false;
        }
        return new Hotkey(keybind.getKeyCode(), keybind.getModifiers()).matches(event);
    }

    @Override
    public void keyTyped(KeyEvent event) {
    }

    @Override
    public void keyReleased(KeyEvent event) {
    }
}
