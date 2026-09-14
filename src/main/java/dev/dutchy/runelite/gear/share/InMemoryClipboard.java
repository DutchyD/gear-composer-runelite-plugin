package dev.dutchy.runelite.gear.share;

import java.util.Optional;

/** A clipboard that lives in this process; for tests and previews. */
public final class InMemoryClipboard implements Clipboard {

    private String text;

    @Override
    public void copy(String newText) {
        text = newText;
    }

    @Override
    public Optional<String> paste() {
        return Optional.ofNullable(text);
    }
}
