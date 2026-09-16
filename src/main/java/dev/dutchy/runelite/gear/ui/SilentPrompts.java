package dev.dutchy.runelite.gear.ui;

import java.util.Optional;

/** Answers nothing and says nothing, for a module that has not been put on screen yet. */
enum SilentPrompts implements Prompts {
    INSTANCE;

    @Override
    public boolean confirm(String question, String title) {
        return false;
    }

    @Override
    public Optional<String> askForName(String question, String initial) {
        return Optional.empty();
    }

    @Override
    public void say(String message) {
    }

    @Override
    public void announce(String message) {
    }
}
