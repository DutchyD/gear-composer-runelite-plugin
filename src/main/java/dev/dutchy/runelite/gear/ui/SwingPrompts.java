package dev.dutchy.runelite.gear.ui;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;
import java.util.Optional;

/** Dialogs over the sidebar, with the status line underneath it. */
final class SwingPrompts implements Prompts {

    private final GearSetupPanel panel;

    private SwingPrompts(GearSetupPanel panel) {
        this.panel = Objects.requireNonNull(panel, "panel");
    }

    static Prompts over(GearSetupPanel panel) {
        return new SwingPrompts(panel);
    }

    @Override
    public boolean confirm(String question, String title) {
        if (GraphicsEnvironment.isHeadless()) {
            return false;
        }
        return JOptionPane.showConfirmDialog(panel, question, title, JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }

    @Override
    public Optional<String> askForName(String question, String initial) {
        if (GraphicsEnvironment.isHeadless()) {
            return Optional.empty();
        }
        return Optional.ofNullable(JOptionPane.showInputDialog(panel, question, initial));
    }

    @Override
    public void say(String message) {
        panel.say(message);
    }

    @Override
    public void announce(String message) {
        panel.announce(message);
    }
}
